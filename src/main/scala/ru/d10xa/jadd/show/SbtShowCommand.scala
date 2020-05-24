package ru.d10xa.jadd.show

import java.nio.file.Path

import cats.data.Chain
import cats.effect.Sync
import cats.implicits._
import coursier.core.Version
import ru.d10xa.jadd.code.scalameta.ModuleIdMatch
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.Scope
import ru.d10xa.jadd.core.types._
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem.Dir
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.versions.ScalaVersions
import ru.d10xa.jadd.instances._

import scala.meta.Source
import scala.meta.Term
import scala.meta.Tree
import scala.meta.dialects
import scala.meta.parsers.Parsed
import scala.meta.transversers.SimpleTraverser

class SbtShowCommand[F[_]: Sync](
  fileOps: FileOps[F],
  scalaVersionFinder: ScalaVersionFinder[F]) {

  def collectNoDuplicate[T](
    tree: Tree,
    fn: PartialFunction[Tree, T]): List[T] = {
    val liftedFn = fn.lift
    val buf = scala.collection.mutable.ListBuffer[T]()
    object traverser extends SimpleTraverser {
      override def apply(tree: Tree): Unit = {
        val x = liftedFn(tree)
        x.foreach(buf += _)
        if (x.isEmpty) super.apply(tree)
      }
    }
    traverser(tree)
    buf.toList
  }

  def show(): F[Chain[Artifact]] =
    Path
      .of("build.sbt")
      .pure[F]
      .flatMap(fileOps.read)
      .flatMap(TextFile.make[F])
      .map(_.content)
      .flatMap(showFromSource)

  def parseSource(s: String): Parsed[Source] =
    dialects
      .Sbt1(s)
      .parse[Source]

  def scalaFilePredicate(p: Path): Boolean = {
    val n = p.getFileName.show
    n.endsWith(".sbt") || n.endsWith(".scala")
  }

  def showFromSource(fileContent: FileContent): F[Chain[Artifact]] = {
    val buildFileSource = fileContent.value
    val scalaVersionF: F[ScalaVersion] =
      scalaVersionFinder
        .findScalaVersion()
        .map(_.getOrElse(ScalaVersions.defaultScalaVersion))

    val otherSbtFilesF = fileOps.read(Path.of("project")).map {
      case Dir(_, names) => names
      case _ => List.empty[Path]
    }

    for {
      scalaVersion <- scalaVersionF
      otherSbtFiles <- otherSbtFilesF
      otherSbtSources <- otherSbtFiles
        .filter(scalaFilePredicate)
        .traverse(fileOps.read)
        .map(_.collect { case t: TextFile => t })
      listOfScalaSourceStrs = buildFileSource :: otherSbtSources.map(
        _.content.value)
      parsedSources = listOfScalaSourceStrs
        .map { str =>
          dialects.Sbt1(str).parse[Source].toEither
        }
        .collect { case Right(value) => value }
      moduleIds = parsedSources
        .flatMap(source =>
          collectNoDuplicate(source, {
            case ModuleIdMatch(m) => m
          }))
        .distinct
      c = Chain.fromSeq(
        moduleIds.map(m =>
          Artifact(
            groupId = GroupId(m.groupId),
            artifactId = if (m.percentsCount > 1) {
              s"${m.artifactId}%%"
            } else m.artifactId,
            maybeVersion = Version(m.version).some,
            maybeScalaVersion = if (m.percentsCount > 1) {
              scalaVersion.some
            } else None,
            scope = m.terms
              .find {
                case Term.Name("Test") => true
                case _ => false
              }
              .map(_ => Scope.Test)
        )))
    } yield c

  }
}
