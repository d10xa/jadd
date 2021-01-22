package ru.d10xa.jadd.pipelines

import cats.syntax.all._
import cats.data.Chain
import cats.effect.Sync
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.versions.ScalaVersions

class UnknownProjectPipeline[F[_]: Sync](
  override val ctx: Ctx
) extends Pipeline[F] {

  def install(
    artifacts: List[Artifact]
  )(implicit logger: Logger[F]): F[Unit] = {

    def stringsToPrint: List[String] =
      artifacts
        .map(_.inlineScalaVersion)
        .map(_.show)

    for {
      _ <- logger.info(
        s"build tool not recognized in directory ${ctx.projectPath}"
      )
      _ <- Sync[F].delay(stringsToPrint.foreach(i => logger.info(i)))
    } yield ()

  }

  override def show()(implicit logger: Logger[F]): F[Chain[Artifact]] =
    logger.info("Unknown project type. Nothing to show") *>
      Chain.empty[Artifact].pure[F]

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
