package ru.d10xa.jadd.pipelines

import better.files._
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.SafeFileWriter
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.GradleShowCommand
import ru.d10xa.jadd.versions.ScalaVersions

class GradlePipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  lazy val buildFile: File = File(ctx.config.projectDir, "build.gradle")

  def buildFileSource[F[_]: Sync]: F[String] =
    Sync[F].delay(buildFile.contentAsString)

  override def applicable[F[_]: Sync](): F[Boolean] =
    Sync[F].delay(buildFile.exists())

  def install[F[_]: Sync](artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource = new GradleFileInserts()
        .appendAll(source, artifacts)
      _ <- Sync[F].delay(new SafeFileWriter().write(buildFile, newSource))
    } yield ()

  override def show[F[_]: Sync](): F[Seq[Artifact]] =
    for {
      source <- buildFileSource
      artifacts = new GradleShowCommand(source).show()
    } yield artifacts

  override def findScalaVersion[F[_]: Sync](): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)
}
