package ru.d10xa.jadd

import better.files._
import ru.d10xa.jadd.analyze.AnalyzeCommandImpl
import ru.d10xa.jadd.cli.Command.Analyze
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.pipelines.GradlePipeline
import ru.d10xa.jadd.pipelines.MavenPipeline
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.pipelines.UnknownProjectPipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl
import cats.implicits._

trait CommandExecutor {
  def execute(config: Config, loader: Loader, showUsage: () => Unit): Unit
}

class CommandExecutorImpl extends CommandExecutor {

  lazy val analyzeCommand = new AnalyzeCommandImpl

  override def execute(
    config: Config,
    loader: Loader,
    showUsage: () => Unit): Unit =
    config match {
      case c if c.command == Repl =>
        () // already in repl
      case c if c.command == Analyze =>
        analyzeCommand.run(Ctx(c))
      case c if c.command == Help =>
        showUsage()
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

  def executePipelines(
    config: Config,
    loader: Loader,
    artifactInfoFinder: ArtifactInfoFinder
  ): Unit = {

    val ctx = Ctx(config)
    val pipelines: List[Pipeline] = List(
      new GradlePipeline(ctx, artifactInfoFinder),
      new MavenPipeline(ctx, artifactInfoFinder),
      new SbtPipeline(
        ctx,
        artifactInfoFinder,
        new ProjectFileReaderImpl(File(config.projectDir))
      )
    )

    val activePipelines =
      Option(pipelines.filter(_.applicable))
        .filter(_.nonEmpty)
        .getOrElse(Seq(new UnknownProjectPipeline(ctx, artifactInfoFinder)))

    activePipelines
      .map(p => p.run(loader))
      .toList
      .sequence_
      .unsafeRunSync()
  }

}
