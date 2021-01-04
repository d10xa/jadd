package ru.d10xa.jadd.pipelines

import cats.syntax.all._
import cats.data.Chain
import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.versions.ScalaVersions

class UnknownProjectPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline[F]
    with StrictLogging {

  def install(artifacts: List[Artifact]): F[Unit] = {
    val logMsg = Sync[F].delay(
      logger.info(s"build tool not recognized in directory ${ctx.projectPath}")
    )

    def stringsToPrint: List[String] =
      artifacts
        .map(_.inlineScalaVersion)
        .map(_.show)

    for {
      _ <- logMsg
      _ <- Sync[F].delay(stringsToPrint.foreach(i => logger.info(i)))
    } yield ()

  }

  override def show(): F[Chain[Artifact]] = {
    val log =
      Sync[F].delay(logger.info("Unknown project type. Nothing to show"))
    log *> Sync[F].pure(Chain.empty)
  }

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
