package ru.d10xa.jadd.pipelines

import cats.data.Ior
import cats.data.IorNel
import cats.effect.IO
import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.show.JaddFormatShowPrinter
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Loader
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.troubles
import cats.implicits._

trait Pipeline extends StrictLogging {
  def applicable: Boolean

  def invokeCommand[F[_]: Sync](
    ior: IorNel[ArtifactTrouble, List[Artifact]],
    action: List[Artifact] => F[Unit]
  ): F[Unit] = {
    val handle: List[ArtifactTrouble] => F[Unit] =
      troubles => Sync[F].delay(handleTroubles(troubles, s => logger.info(s)))

    ior match {
      case Ior.Right(artifacts) =>
        action(artifacts)
      case Ior.Left(troubles) =>
        handle(troubles.toList)
      case Ior.Both(troubles, artifacts) =>
        action(artifacts) >> handle(troubles.toList)
    }
  }

  def handleInstall[F[_]: Sync](
    ior: IorNel[ArtifactTrouble, List[Artifact]]
  ): F[Unit] =
    invokeCommand(ior, artifacts => Sync[F].delay(install(artifacts)))

  def handleSearch[F[_]: Sync](
    ior: IorNel[ArtifactTrouble, List[Artifact]]
  ): F[Unit] =
    invokeCommand(ior, artifacts => Sync[F].delay(search(artifacts)))

  def run(
    loader: Loader
  ): IO[Unit] = {
    val loaded: IO[IorNel[troubles.ArtifactTrouble, List[Artifact]]] =
      loader.load[IO](ctx)
    ctx.config.command match {
      case Show =>
        IO(show())
          .map(_.toList)
          .map(ctx.config.showPrinter.mkString(_))
          .map(s => logger.info(s))
      case Search =>
        loaded.flatMap(handleSearch[IO])
      case Install =>
        loaded.flatMap(handleInstall[IO])
      case command =>
        IO(logger.info(s"command $command not implemented"))
    }
  }

  def search(artifacts: List[Artifact]): Unit = {
    val artifactsWithVersions = artifacts.map(_.inlineScalaVersion)
    logger.info(ctx.config.showPrinter.mkString(artifactsWithVersions))
    val stringsForPrint = artifactsWithVersions
      .map(
        artifact =>
          JaddFormatShowPrinter
            .single(artifact) + " // " + artifact.versionsForPrint)
    logger.info(stringsForPrint.mkString("\n"))
  }

  def install(artifacts: List[Artifact]): Unit
  def show(): Seq[Artifact]
  def ctx: Ctx

}

object Pipeline {

  def extractArtifacts(ctx: Ctx): Seq[String] =
    if (ctx.config.requirements.nonEmpty) {
      for {
        requirement <- ctx.config.requirements
        source = Utils.mkStringFromResource(requirement)
        artifact <- source.trim.split("\\r?\\n").map(_.trim)
      } yield artifact
    } else {
      ctx.config.artifacts
    }

}
