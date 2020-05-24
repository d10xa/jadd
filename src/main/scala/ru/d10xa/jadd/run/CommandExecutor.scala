package ru.d10xa.jadd.run

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import ru.d10xa.jadd.buildtools.BuildToolLayout
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveSbtScalaVersionFinder
import ru.d10xa.jadd.core.Loader
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.pipelines._
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl
import ru.d10xa.jadd.show.SbtShowCommand

trait CommandExecutor[F[_]] {
  def execute(
    ctx: Ctx,
    loader: Loader[F],
    fileOps: FileOps[F],
    showUsage: () => Unit): F[Unit]
}

class LiveCommandExecutor[F[_]: Sync] private (
  buildToolLayoutSelector: BuildToolLayoutSelector[F])
    extends CommandExecutor[F] {

  override def execute(
    ctx: Ctx,
    loader: Loader[F],
    fileOps: FileOps[F],
    showUsage: () => Unit): F[Unit] =
    ctx.config match {
      case c if c.command == Repl =>
        Applicative[F].unit // already in repl
      case c if c.command == Help =>
        Sync[F].delay(showUsage())
      case _ =>
        val repositoryShortcuts = RepositoryShortcutsImpl
        val artifactInfoFinder: ArtifactInfoFinder =
          new ArtifactInfoFinder(
            artifactShortcuts = new ArtifactShortcuts(
              Utils.sourceFromSpringUri(ctx.config.shortcutsUri)),
            repositoryShortcuts = repositoryShortcuts
          )
        executePipelines(ctx, loader, fileOps, artifactInfoFinder)
    }

  private def executePipelines(
    ctx: Ctx,
    loader: Loader[F],
    fileOps: FileOps[F],
    artifactInfoFinder: ArtifactInfoFinder
  ): F[Unit] = {

    val pipeline: F[Pipeline[F]] = for {
      layout <- buildToolLayoutSelector.select(ctx)
    } yield {
      layout match {
        case BuildToolLayout.Gradle =>
          new GradlePipeline(ctx, artifactInfoFinder, fileOps)
        case BuildToolLayout.Maven =>
          new MavenPipeline(ctx, artifactInfoFinder, fileOps)
        case BuildToolLayout.Sbt =>
          val scalaVersionFinder = LiveSbtScalaVersionFinder.make(ctx, fileOps)
          new SbtPipeline(
            ctx,
            artifactInfoFinder,
            scalaVersionFinder,
            new SbtShowCommand(
              fileOps,
              scalaVersionFinder
            ),
            fileOps
          )
        case BuildToolLayout.Ammonite =>
          new AmmonitePipeline(ctx, fileOps)
        case BuildToolLayout.Unknown =>
          new UnknownProjectPipeline(ctx, artifactInfoFinder)
      }
    }

    pipeline.flatMap(_.run(loader))
  }

}

object LiveCommandExecutor {
  def make[F[_]: Sync](
    buildToolLayoutSelector: BuildToolLayoutSelector[F]): CommandExecutor[F] =
    new LiveCommandExecutor(buildToolLayoutSelector)
}
