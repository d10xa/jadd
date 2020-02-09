package ru.d10xa.jadd.pipelines

import better.files._
import cats.data.Chain
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.ProjectFileReader
import ru.d10xa.jadd.core.SafeFileWriter
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  scalaVersionFinder: ScalaVersionFinder[F],
  projectFileReader: ProjectFileReader[F])
    extends Pipeline[F]
    with StrictLogging {

  val buildFileName = "build.sbt"

  def buildFile: F[File] =
    projectFileReader.file(buildFileName)

  def buildFileSource: F[String] =
    buildFile.map(_.contentAsString)

  override def applicable(): F[Boolean] =
    projectFileReader.exists(buildFileName)

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource: String = new SbtFileInserts().appendAll(source, artifacts)
      _ <- fileUpdate(newSource)
    } yield ()

  def fileUpdate(str: String): F[Unit] = buildFile.map { f =>
    new SafeFileWriter().write(f, str)
  }

  override def show(): F[Chain[Artifact]] =
    for {
      artifacts <- new SbtShowCommand(
        projectFileReader,
        scalaVersionFinder
      ).show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    scalaVersionFinder.findScalaVersion()
}
