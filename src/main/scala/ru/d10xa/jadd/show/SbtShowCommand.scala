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
    for {
      sbtFiles <- sbtFilesF
      sbtSources <- sbtFiles
        .filter(scalaFilePredicate)
        .traverse(fileOps.read)
        .map(_.collect { case t: TextFile => t })
      res <- showFromTextFiles(sbtSources)
    } yield res

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

  val sbtFilesF: F[List[Path]] = for {
    buildSbt <- Paths.get("build.sbt").pure[F]
    other <- otherSbtFilesF
  } yield buildSbt :: other

  def showFromTextFiles(files: List[TextFile]): F[Chain[Artifact]] =
    for {
      scalaVersion <- scalaVersionF
      listOfScalaSourceStrs = files.map(_.content.value)
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
