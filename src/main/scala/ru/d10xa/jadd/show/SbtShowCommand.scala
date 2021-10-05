package ru.d10xa.jadd.show

import java.nio.file.Path
import java.nio.file.Paths
import cats.syntax.all._
import cats.data.Chain
import cats.effect.Sync
import ru.d10xa.jadd.code.SbtFileUtils
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
  sbtFileUtils: SbtFileUtils[F],
  fileOps: FileOps[F],
  scalaVersionFinder: ScalaVersionFinder[F],
  sbtArtifactsParser: SbtArtifactsParser[F]
) {

  def show(): F[Chain[Artifact]] =
    for {
      sbtFiles <- sbtFileUtils.sbtFiles
      sbtSources <- sbtFiles
        .traverse(fileOps.read)
        .map(_.collect { case t: TextFile => t })
      res <- showFromTextFiles(sbtSources)
    } yield res

  def parseSource(s: String): Parsed[Source] =
    dialects
      .Sbt1(s)
      .parse[Source]

  val scalaVersionF: F[ScalaVersion] = scalaVersionFinder
    .findScalaVersion()
    .map(_.getOrElse(ScalaVersions.defaultScalaVersion))

  def showFromTextFiles(files: List[TextFile]): F[Chain[Artifact]] =
    for {
      scalaVersion <- scalaVersionF
      parsedSources = files
        .map { textFile =>
          dialects
            .Sbt1(textFile.content.value)
            .parse[Source]
            .toEither
            .map(source => textFile -> source)
        }
        .collect { case Right((textFile, source)) => (textFile, source) }
      c <- sbtArtifactsParser.parseArtifacts(
        scalaVersion,
        parsedSources.map { case (textFile, source) =>
          textFile.path -> source
        }.toVector
      )
    } yield Chain.fromSeq(c)

}
