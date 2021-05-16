package ru.d10xa.jadd.show

import java.nio.file.Path
import java.nio.file.Paths
import cats.syntax.all._
import cats.data.Chain
import cats.effect.Sync
import ru.d10xa.jadd.code.scalameta.SbtArtifactsParser
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.types._
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem.Dir
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.versions.ScalaVersions
import ru.d10xa.jadd.instances._

import scala.meta.Source
import scala.meta.dialects
import scala.meta.parsers.Parsed

class SbtShowCommand[F[_]: Sync](
  fileOps: FileOps[F],
  scalaVersionFinder: ScalaVersionFinder[F],
  sbtArtifactsParser: SbtArtifactsParser[F]
) {

  def show(): F[Chain[Artifact]] =
    Paths
      .get("build.sbt")
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

  val scalaVersionF: F[ScalaVersion] = scalaVersionFinder
    .findScalaVersion()
    .map(_.getOrElse(ScalaVersions.defaultScalaVersion))

  val otherSbtFilesF: F[List[Path]] = fileOps.read(Paths.get("project")).map {
    case Dir(_, names) => names
    case _ => List.empty[Path]
  }

  def showFromSource(fileContent: FileContent): F[Chain[Artifact]] =
    for {
      scalaVersion <- scalaVersionF
      otherSbtFiles <- otherSbtFilesF
      otherSbtSources <- otherSbtFiles
        .filter(scalaFilePredicate)
        .traverse(fileOps.read)
        .map(_.collect { case t: TextFile => t })
      listOfScalaSourceStrs = fileContent.value :: otherSbtSources.map(
        _.content.value
      )
      parsedSources = listOfScalaSourceStrs
        .map { str =>
          dialects.Sbt1(str).parse[Source].toEither
        }
        .collect { case Right(value) => value }
      c <- sbtArtifactsParser.parseArtifacts(
        scalaVersion,
        parsedSources.toVector
      )
    } yield Chain.fromSeq(c)

}
