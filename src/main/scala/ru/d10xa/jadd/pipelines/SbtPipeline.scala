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
import ru.d10xa.jadd.show.SbtShowCommand2

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  scalaVersionFinder: ScalaVersionFinder[F],
  showCommand: SbtShowCommand2[F],
  fileOps: FileOps[F])
    extends Pipeline[F]
    with StrictLogging {

  val buildFileName: FileName = FileName("build.sbt")

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
    fileOps.write(buildFileName, str)

  override def show(): F[Chain[Artifact]] =
    for {
      artifacts <- showCommand.show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    scalaVersionFinder.findScalaVersion()

}
