package ru.d10xa.jadd.show

import cats.Applicative
import cats.data.Chain
import cats.effect.Sync
import cats.implicits._
import coursier.core.Version
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.d10xa.jadd.code.regex.SbtVerbalExpressions
import ru.d10xa.jadd.core
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.CodeBlock
import ru.d10xa.jadd.core.ProjectFileReader
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.Scope
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.generated.antlr.SbtDependenciesBaseVisitor
import ru.d10xa.jadd.generated.antlr.SbtDependenciesLexer
import ru.d10xa.jadd.generated.antlr.SbtDependenciesParser
import ru.d10xa.jadd.versions.ScalaVersions

import scala.jdk.CollectionConverters._

class SbtShowCommand[F[_]: Sync](
  projectFileReader: ProjectFileReader[F],
  scalaVersionFinder: ScalaVersionFinder[F]) {
  import SbtShowCommand._

  def show(): F[Chain[Artifact]] =
    projectFileReader.read("build.sbt").flatMap(showFromSource)

  def showFromSource(buildFileSource: String): F[Chain[Artifact]] = {

    val blocks: Seq[CodeBlock] = Seq("Seq", "List", "Vector")
      .map(s => s"libraryDependencies ++= $s(")
      .flatMap(CodeBlock.find(buildFileSource, _))

    def lexer(blockInner: String): SbtDependenciesLexer =
      new SbtDependenciesLexer(CharStreams.fromString(blockInner))

    def parser(lexer: SbtDependenciesLexer): SbtDependenciesParser =
      new SbtDependenciesParser(new CommonTokenStream(lexer))

    val scalaVersionF: F[ScalaVersion] =
      scalaVersionFinder
        .findScalaVersion()
        .map(_.getOrElse(ScalaVersions.defaultScalaVersion))

    val multiple: ScalaVersion => Seq[Artifact] = scalaVersion =>
      blocks.map(_.innerContent).flatMap { innerContent =>
        val p = parser(lexer(innerContent))
        val v = new LibraryDependenciesVisitor(scalaVersion)
        v.visitMultipleDependencies(p.multipleDependencies())
    }

    val list: List[String] =
      SbtVerbalExpressions.singleLibraryDependency
        .getTextGroups(buildFileSource, 1)
        .asScala
        .toList

    val visitor0: ScalaVersion => SingleDependencyVisitor = scalaVersion =>
      new SingleDependencyVisitor(scalaVersion)

    val single0: SingleDependencyVisitor => List[Artifact] = visitor =>
      list
        .map(lexer)
        .map(parser)
        .map(_.singleDependency())
        .flatMap(visitor.visitSingleDependency(_).toList)

    val fromDependenciesExternalFile: ScalaVersion => F[Chain[Artifact]] =
      scalaVersion =>
        if (buildFileSource.contains("import Dependencies._")) {
          val tupleToArtifact
            : ((String, String, String, String)) => Artifact = {
            case (gId, "%%", aId, version) =>
              core.Artifact(
                groupId = GroupId(gId),
                artifactId = s"$aId%%",
                maybeVersion = Some(Version(version)),
                maybeScalaVersion = Some(scalaVersion))
            case (gId, _, aId, version) =>
              Artifact(
                groupId = GroupId(gId),
                artifactId = aId,
                maybeVersion = Some(Version(version)),
                maybeScalaVersion = None
              )
          }
          import ru.d10xa.jadd.code.regex.RegexImplicits._
          val p: F[String] = projectFileReader
            .read("project/Dependencies.scala")
          p.map { source =>
              SbtVerbalExpressions.declaredDependency.groups4(source)
            }
            .map {
              _.map(tupleToArtifact)
            }
            .map(Chain.fromSeq)
        } else {
          Applicative[F].pure(Chain.empty[Artifact])
      }

    for {
      scalaVersion <- scalaVersionF
      visitor <- Sync[F].delay(visitor0(scalaVersion))
      single <- Sync[F].delay(single0(visitor))
      multiple <- Sync[F].delay(multiple(scalaVersion))
      fromFile <- fromDependenciesExternalFile(scalaVersion)
    } yield Chain.fromSeq((single ++ multiple ++ fromFile.toList).distinct)
  }
}

object SbtShowCommand {

  class LibraryDependenciesVisitor(scalaVersion: ScalaVersion)
      extends SbtDependenciesBaseVisitor[List[Artifact]] {

    override def visitMultipleDependencies(
      ctx: SbtDependenciesParser.MultipleDependenciesContext
    ): List[Artifact] = {
      val v = new SingleDependencyVisitor(scalaVersion)
      ctx
        .singleDependency()
        .asScala
        .toList
        .flatMap(v.visitSingleDependency(_).toList)
    }
  }

  class SingleDependencyVisitor(scalaVersion: ScalaVersion)
      extends SbtDependenciesBaseVisitor[Option[Artifact]] {
    override def visitSingleDependency(
      ctx: SbtDependenciesParser.SingleDependencyContext
    ): Option[Artifact] = {
      val arr: List[String] =
        ctx
          .ScalaString()
          .asScala
          .toList
          .map(_.getText)
          .filter(_ != null)
          .filter(_.startsWith("\""))
          .filter(_.endsWith("\""))
          .map(_.drop(1).dropRight(1))
      val isScala =
        Option(ctx.percents())
          .map(_.getText)
          .exists(_.length == 2)

      val scope: Option[Scope] =
        Option(ctx.Scope())
          .map(_.getText)
          .map(_.replaceAll("\"", ""))
          .map(_.toLowerCase)
          .map(_ == "test")
          .flatMap(if (_) Some(Scope.Test) else None)

      arr match {
        case g :: a :: v :: Nil =>
          Some(
            Artifact(
              groupId = GroupId(g),
              artifactId = if (isScala) s"$a%%" else a,
              maybeVersion = Some(Version(v)),
              maybeScalaVersion = if (isScala) Some(scalaVersion) else None,
              scope = scope
            ))
        case _ => None
      }
    }
  }

  class PercentsVisitor extends SbtDependenciesBaseVisitor[Int] {
    override def visitPercents(
      ctx: SbtDependenciesParser.PercentsContext
    ): Int =
      ctx.getText.length
  }
}
