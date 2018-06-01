package ru.d10xa.jadd

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory
import ru.d10xa.jadd.Cli.Analyze
import ru.d10xa.jadd.Cli.Config
import ru.d10xa.jadd.Cli.Help
import ru.d10xa.jadd.Cli.Repl
import ru.d10xa.jadd.pipelines.GradlePipeline
import ru.d10xa.jadd.pipelines.MavenPipeline
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.pipelines.UnknownProjectPipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl

object Main extends LazyLogging {

  def run(config: Config): Unit = {
    implicit val artifactInfoFinder: ArtifactInfoFinder =
      new ArtifactInfoFinder(
        artifactShortcuts = new ArtifactShortcuts(Utils.sourceFromSpringUri(config.shortcutsUri)),
        repositoryShortcuts = RepositoryShortcutsImpl
      )
    val ctx = Ctx(config)
    val pipelines: List[Pipeline] = List(
      new GradlePipeline(ctx),
      new MavenPipeline(ctx),
      new SbtPipeline(ctx)
    )
    val activePipelines = pipelines.filter(_.applicable)

    if (activePipelines.isEmpty) new UnknownProjectPipeline(ctx).run()
    else activePipelines.foreach(_.run())
  }

  def main(args: Array[String]): Unit = {
    readConfig(args) match {
      case Some(config) if config.command == Repl =>
        ReplCommand.runRepl(runOnceForRepl)
      case Some(config) =>
        runOnce(args, config)
      case None =>
        logger.error("arguments are bad")
    }
  }

  def readConfig(args: Array[String]): Option[Config] = {
    val maybeConfig: Option[Config] =
      Cli.parser.parse(args, Cli.Config())
    maybeConfig.foreach { cfg =>
      if(cfg.debug) enableDebugMode()
    }
    maybeConfig
  }

  def runOnceForRepl(args: Array[String]): Unit = {
    // TODO reduce copy/paste from main
    readConfig(args) match {
      case Some(config) =>
        runOnce(args, config)
      case None =>
        logger.error("arguments are bad")
    }
  }

  def runOnce(args: Array[String], config: Config): Unit = {
    config match {
      case c if c.command == ReplCommand =>
        Unit // already in repl
      case c if c.command == Analyze =>
        analyze.run(Ctx(c))
      case c if c.command == Help =>
        Cli.parser.showUsage()
      case c =>
        Main.run(c)
    }
  }

  def enableDebugMode(): Unit = {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = loggerContext.getLogger("ru.d10xa.jadd")
    rootLogger.setLevel(Level.DEBUG)
    logger.debug("Debug mode enabled")
  }

}
