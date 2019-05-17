package ru.d10xa.jadd.pipelines

import cats.effect.IO
import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Loader.Result
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.show.JaddFormatShowPrinter
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.view.ArtifactView
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Loader
import ru.d10xa.jadd.Utils

trait Pipeline extends StrictLogging {
  def applicable: Boolean
  def handleInstall[F[_]: Sync](r: Result): F[Unit] =
    Sync[F].delay {
      install(r.values)
      handleTroubles(r.errors.flatMap(_.toList), s => logger.info(s))
    }

  def handleSearch[F[_]: Sync](r: Result): F[Unit] =
    Sync[F].delay {
      search(r.values)
      handleTroubles(r.errors.flatMap(_.toList), s => logger.info(s))
    }

  def run(
    loader: Loader
  ): IO[Unit] = {
    val loaded: IO[Result] = loader.load[IO](ctx)
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
  def asPrintLines[A](a: A)(implicit view: ArtifactView[A]): Seq[String] =
    view.showLines(a)

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
