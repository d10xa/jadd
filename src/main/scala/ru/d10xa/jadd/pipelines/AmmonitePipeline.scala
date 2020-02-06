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
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.show.AmmoniteFormatShowPrinter
import ru.d10xa.jadd.versions.ScalaVersions

class AmmonitePipeline[F[_]: Sync](
  override val ctx: Ctx,
  projectFileReader: ProjectFileReader
) extends Pipeline[F]
    with StrictLogging {

  def buildFile: F[File] =
    projectFileReader.file(ctx.config.projectDir)

  def buildFileSource: F[String] =
    buildFile.map(_.contentAsString)

  override def applicable(): F[Boolean] =
    for {
      file <- projectFileReader.file(ctx.config.projectDir)
      exists = file.exists
      isScalaScript = file.name.endsWith(".sc")
    } yield exists && isScalaScript

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      newDependencies <- Sync[F].delay(
        AmmoniteFormatShowPrinter.mkString(artifacts))
      file <- buildFile
      source <- buildFileSource
      newSource = Seq(newDependencies, source).mkString("\n")
      _ <- Sync[F].delay(new SafeFileWriter().write(file, newSource))
    } yield ()

  // TODO implement
  override def show(): F[Chain[Artifact]] =
    Sync[F].delay(???)

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
