package ru.d10xa.jadd.pipelines

import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.versions.ScalaVersions

class UnknownProjectPipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  /**
    * used only if project directory is unrecognized
    */
  override def applicable[F[_]: Sync](): F[Boolean] =
    Sync[F].pure(true)

  def install[F[_]: Sync](artifacts: List[Artifact]): F[Unit] = {
    val logMsg = Sync[F].delay(
      logger.info(
        s"build tool not recognized in directory ${ctx.config.projectDir}"))

    def stringsToPrint: List[String] =
      artifacts
        .map(_.inlineScalaVersion)
        .map(_.show)

    for {
      _ <- logMsg
      _ <- Sync[F].delay(stringsToPrint.foreach(i => logger.info(i)))
    } yield ()

  }

  override def show[F[_]: Sync](): F[Seq[Artifact]] = {
    val log =
      Sync[F].delay(logger.info("Unknown project type. Nothing to show"))
    val emptySeq: F[Seq[Artifact]] = Sync[F].pure(Seq.empty)
    log *> emptySeq
  }

  override def findScalaVersion[F[_]: Sync](): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
