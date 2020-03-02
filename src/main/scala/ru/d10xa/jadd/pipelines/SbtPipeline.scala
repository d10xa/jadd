package ru.d10xa.jadd.pipelines

import cats.data.Chain
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.code.inserts.SbtFileInserts
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  scalaVersionFinder: ScalaVersionFinder[F],
  fileOps: FileOps[F])
    extends Pipeline[F]
    with StrictLogging {

  val buildFileName = "build.sbt"

  def buildFileSource: F[TextFile] =
    for {
      textFile <- Utils.textFileFromString(fileOps, buildFileName)
      source = textFile
    } yield source

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource: String = new SbtFileInserts()
        .appendAll(source.content.value, artifacts)
      _ <- fileUpdate(newSource)
    } yield ()

  def fileUpdate(str: String): F[Unit] =
    for {
      fileName <- FileName.make[F](buildFileName)
      _ <- fileOps.write(fileName, str)
    } yield ()

  override def show(): F[Chain[Artifact]] =
    for {
      artifacts <- new SbtShowCommand(
        fileOps,
        scalaVersionFinder
      ).show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    scalaVersionFinder.findScalaVersion()

}
