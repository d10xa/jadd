package ru.d10xa.jadd.run

import java.nio.file.Paths

import better.files._
import cats.Applicative
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveSbtScalaVersionFinder
import ru.d10xa.jadd.core.Loader
import ru.d10xa.jadd.core.ProjectFileReaderImpl
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.fs.LiveCachedFileOps
import ru.d10xa.jadd.fs.LiveFileOps
import ru.d10xa.jadd.pipelines._
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl

trait CommandExecutor[F[_]] {
  def execute(config: Config, loader: Loader[F], showUsage: () => Unit): F[Unit]
}

class LiveCommandExecutor[F[_]: Sync] extends CommandExecutor[F] {

  override def execute(
    config: Config,
    loader: Loader[F],
    showUsage: () => Unit): F[Unit] =
    config match {
      case c if c.command == Repl =>
        Applicative[F].unit // already in repl
      case c if c.command == Help =>
        Sync[F].delay(showUsage())
      case c =>
        val repositoryShortcuts = RepositoryShortcutsImpl
        val artifactInfoFinder: ArtifactInfoFinder =
          new ArtifactInfoFinder(
            artifactShortcuts = new ArtifactShortcuts(
              Utils.sourceFromSpringUri(config.shortcutsUri)),
            repositoryShortcuts = repositoryShortcuts
          )
        executePipelines(c, loader, artifactInfoFinder)
    }

  private def executePipelines(
    config: Config,
    loader: Loader[F],
    artifactInfoFinder: ArtifactInfoFinder
  ): F[Unit] = {

    val ctx = Ctx(config)

    val projectFileReaderImpl = new ProjectFileReaderImpl(
      File(config.projectDir))
    val fileOpsF = LiveFileOps.make(Paths.get(config.projectDir))

    val pipelines: F[List[Pipeline[F]]] = for {
      cacheRef <- Ref.of[F, FileCache](FileCache.empty)
      fileOps <- fileOpsF
      fileOpsCached <- LiveCachedFileOps.make(fileOps, cacheRef)
      scalaVersionFinder = LiveSbtScalaVersionFinder.make(ctx, fileOpsCached)
    } yield {
      List(
        new GradlePipeline(ctx, artifactInfoFinder),
        new MavenPipeline(ctx, artifactInfoFinder),
        new SbtPipeline(
          ctx,
          artifactInfoFinder,
          scalaVersionFinder,
          fileOps
        ),
        new AmmonitePipeline(ctx, projectFileReaderImpl)
      )
    }

    def orDefaultPipeline(
      pipelines: List[Pipeline[F]]): NonEmptyList[Pipeline[F]] =
      NonEmptyList
        .fromList(pipelines)
        .getOrElse(
          NonEmptyList.of(new UnknownProjectPipeline(ctx, artifactInfoFinder)))

    def runPipelines(pipelines: NonEmptyList[Pipeline[F]]): F[Unit] =
      pipelines.map(_.run(loader)).sequence_

    def activePipelines(): F[NonEmptyList[Pipeline[F]]] =
      pipelines
        .flatMap(filterPipelines)
        .map(orDefaultPipeline)

    def runActivePipelines(): F[Unit] =
      activePipelines()
        .flatMap(runPipelines)

    runActivePipelines()

  }

  def filterPipelines(
    pipelines: List[Pipeline[F]]
  ): F[List[Pipeline[F]]] =
    pipelines
    // TODO recover is not safe
      .map(p => p.applicable().recover { case _ => false }.map(_ -> p))
      .sequence
      .map(_.collect { case (b, p) if b => p })

}

object LiveCommandExecutor {
  def make[F[_]: Sync](): CommandExecutor[F] = new LiveCommandExecutor()
}
