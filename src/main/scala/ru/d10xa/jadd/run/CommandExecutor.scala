package ru.d10xa.jadd.run

import cats.syntax.all._
import cats.Applicative
import cats.effect.Sync
import ru.d10xa.jadd.buildtools.BuildToolLayout
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.code.scalameta.SbtArtifactsParser
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveSbtScalaVersionFinder
import ru.d10xa.jadd.core.Loader
import ru.d10xa.jadd.coursier_.CoursierVersions
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.pipelines._
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.show.SbtShowCommand
import ru.d10xa.jadd.versions.VersionTools

trait CommandExecutor[F[_]] {
  def execute(
    ctx: Ctx,
    loader: Loader[F],
    fileOps: FileOps[F],
    showUsage: () => Unit,
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[Unit]
}

class CommandExecutorImpl[F[_]: Sync] private (
  coursierVersions: CoursierVersions[F],
  buildToolLayoutSelector: BuildToolLayoutSelector[F]
) extends CommandExecutor[F] {

  override def execute(
    ctx: Ctx,
    loader: Loader[F],
    fileOps: FileOps[F],
    showUsage: () => Unit,
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[Unit] =
    ctx.config match {
      case c if c.command == Repl =>
        Applicative[F].unit // already in repl
      case c if c.command == Help =>
        Sync[F].delay(showUsage())
      case _ =>
        executePipelines(ctx, loader, fileOps, artifactShortcuts)
    }

  def layoutToPipeline(
    layout: BuildToolLayout,
    ctx: Ctx,
    fileOps: FileOps[F]
  ): F[Pipeline[F]] =
    layout match {
      case BuildToolLayout.Gradle =>
        Sync[F].delay(new GradlePipeline(ctx, fileOps))
      case BuildToolLayout.Maven =>
        Sync[F].delay(new MavenPipeline(ctx, fileOps))
      case BuildToolLayout.Sbt =>
        for {
          sbtArtifactsParser <- SbtArtifactsParser.make[F]()
          scalaVersionFinder = LiveSbtScalaVersionFinder.make(ctx, fileOps)
          sbtPipeline = new SbtPipeline(
            ctx,
            scalaVersionFinder,
            new SbtShowCommand(
              fileOps,
              scalaVersionFinder,
              sbtArtifactsParser
            ),
            fileOps
          )
        } yield sbtPipeline
      case BuildToolLayout.Ammonite =>
        Sync[F].delay(new AmmonitePipeline(ctx, fileOps))
      case BuildToolLayout.Unknown =>
        Sync[F].delay(new UnknownProjectPipeline(ctx))
    }

  private def executePipelines(
    ctx: Ctx,
    loader: Loader[F],
    fileOps: FileOps[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[Unit] = {
    val pipeline: F[Pipeline[F]] = for {
      layout <- buildToolLayoutSelector.select(ctx)
      p <- layoutToPipeline(layout, ctx, fileOps)
    } yield p
    val versionTools = VersionTools.make[F](coursierVersions)
    pipeline.flatMap(_.run(loader, versionTools, artifactShortcuts))
  }

}

object CommandExecutorImpl {
  def make[F[_]: Sync](
    coursierVersions: CoursierVersions[F],
    buildToolLayoutSelector: BuildToolLayoutSelector[F]
  ): CommandExecutor[F] =
    new CommandExecutorImpl(
      coursierVersions,
      buildToolLayoutSelector
    )
}
