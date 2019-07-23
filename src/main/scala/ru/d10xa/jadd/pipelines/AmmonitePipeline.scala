package ru.d10xa.jadd.pipelines

import better.files._
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.ProjectFileReader
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.show.AmmoniteFormatShowPrinter
import ru.d10xa.jadd.versions.ScalaVersions

class AmmonitePipeline(
  override val ctx: Ctx,
  projectFileReader: ProjectFileReader
) extends Pipeline
    with StrictLogging {

  def buildFile[F[_]: Sync]: F[File] =
    projectFileReader.file(ctx.config.projectDir)

  def buildFileSource[F[_]: Sync]: F[String] =
    buildFile.map(_.contentAsString)

  override def applicable[F[_]: Sync](): F[Boolean] =
    for {
      file <- projectFileReader.file(ctx.config.projectDir)
      exists = file.exists
      isScalaScript = file.name.endsWith(".sc")
    } yield exists && isScalaScript

  def install[F[_]: Sync](artifacts: List[Artifact]): F[Unit] =
    for {
      newDependencies <- Sync[F].delay(
        AmmoniteFormatShowPrinter.mkString(artifacts))
      file <- buildFile
      source <- buildFileSource
      newSource = Seq(newDependencies, source).mkString("\n")
      _ <- Sync[F].delay(new SafeFileWriter().write(file, newSource))
    } yield ()

  // TODO implement
  override def show[F[_]: Sync](): F[Seq[Artifact]] =
    Sync[F].delay(???)

  override def findScalaVersion[F[_]: Sync](): F[Option[String]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
