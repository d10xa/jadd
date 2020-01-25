package ru.d10xa.jadd.show

import cats.effect.IO
import coursier.core.Version
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.d10xa.jadd.core
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.CodeBlock
import ru.d10xa.jadd.core.GroupId
import ru.d10xa.jadd.core.ProjectFileReader
import ru.d10xa.jadd.core.ScalaVersion
import ru.d10xa.jadd.core.Scope
import ru.d10xa.jadd.generated.antlr.SbtDependenciesBaseVisitor
import ru.d10xa.jadd.generated.antlr.SbtDependenciesLexer
import ru.d10xa.jadd.generated.antlr.SbtDependenciesParser
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.code.regex.SbtVerbalExpressions
import ru.d10xa.jadd.versions.ScalaVersions

import scala.jdk.CollectionConverters._

class SbtShowCommand(
  buildFileSource: String,
  projectFileReader: ProjectFileReader,
  config: Config) {
  import SbtShowCommand._

  lazy val scalaVersionFromBuildSbt: Option[ScalaVersion] = SbtPipeline
    .extractScalaVersionFromBuildSbt(buildFileSource)

  def show(): Seq[Artifact] = {

    val blocks: Seq[CodeBlock] = Seq("Seq", "List", "Vector")
      .map(s => s"libraryDependencies ++= $s(")
      .flatMap(CodeBlock.find(buildFileSource, _))

    def lexer(blockInner: String): SbtDependenciesLexer =
      new SbtDependenciesLexer(CharStreams.fromString(blockInner))

    def parser(lexer: SbtDependenciesLexer): SbtDependenciesParser =
      new SbtDependenciesParser(new CommonTokenStream(lexer))

    val scalaVersion =
      config.scalaVersion
        .orElse(scalaVersionFromBuildSbt)
        .getOrElse(ScalaVersions.defaultScalaVersion)

    val multiple: Seq[Artifact] = blocks.map(_.innerContent).flatMap {
      innerContent =>
        val p = parser(lexer(innerContent))
        val v = new LibraryDependenciesVisitor(scalaVersion)
        v.visitMultipleDependencies(p.multipleDependencies())
    }

    val list: List[String] =
      SbtVerbalExpressions.singleLibraryDependency
        .getTextGroups(buildFileSource, 1)
        .asScala
        .toList

    val visitor = new SingleDependencyVisitor(scalaVersion)

    val single = list
      .map(lexer)
      .map(parser)
      .map(_.singleDependency())
      .flatMap(visitor.visitSingleDependency(_).toList)

    val fromDependenciesExternalFile: Seq[Artifact] =
      if (buildFileSource.contains("import Dependencies._")) {
        val tupleToArtifact: ((String, String, String, String)) => Artifact = {
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
        projectFileReader
          .read[IO]("project/Dependencies.scala")
          .map { source =>
            SbtVerbalExpressions.declaredDependency.groups4(source)
          }
          .map { _.map(tupleToArtifact) }
          .unsafeRunSync()
      } else {
        Seq.empty
      }

    (multiple ++ single ++ fromDependenciesExternalFile).distinct
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
