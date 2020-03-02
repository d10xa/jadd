package ru.d10xa.jadd.run

import java.net.URI
import java.nio.file.Paths

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.StrictLogging
import cats.implicits._
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveLoader
import ru.d10xa.jadd.core.ProxySettings
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.LiveCachedFileOps
import ru.d10xa.jadd.fs.LiveFileOps
import ru.d10xa.jadd.repl.ReplCommand
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl
import ru.d10xa.jadd.log.LoggingUtil

class JaddRunner[F[_]: Sync](
  cli: Cli,
  loggingUtil: LoggingUtil
) {

  private def readAndEvalConfig(args: Vector[String]): Config = {
    val config = cli.parse(args)
    if (config.debug) loggingUtil.enableDebug()
    if (config.quiet) loggingUtil.quiet()
    config.proxy.foreach(initProxy)
    config
  }

  private def initProxy(proxyStr: String): Unit = {
    val proxyUri = new URI(proxyStr)
    val proxySettings = ProxySettings.fromURI(proxyUri)
    ProxySettings.set(proxySettings)
    (proxySettings.httpProxyUser, proxySettings.httpProxyPassword) match {
      case (Some(u), Some(p)) => ProxySettings.setupAuthenticator(u, p)
      case _ => // do nothing
    }
  }

  private def runOnceForRepl(runParams: RunParams[F]): F[Unit] = {
    val config: Config = readAndEvalConfig(runParams.args)
    val ctx = Ctx(config)
    for {
      fileOps <- paramsToFileOps(runParams)
      layoutSelector = BuildToolLayoutSelector.make[F](fileOps)
      commandExecutor = LiveCommandExecutor.make[F](layoutSelector)
      _ <- JaddRunner.runOnce[F](ctx, fileOps, commandExecutor)
    } yield ()
  }

  def paramsToFileOps(runParams: RunParams[F]): F[FileOps[F]] = {
    val config: Config = readAndEvalConfig(runParams.args)
    val ctx = Ctx(config)
    for {
      cacheRef <- Ref.of[F, FileCache](FileCache.empty)
      ops <- LiveFileOps.make(Paths.get(ctx.config.projectDir))
      cachedOps <- LiveCachedFileOps.make(ops, cacheRef)
    } yield cachedOps
  }

  def run(runParams: RunParams[F]): F[Unit] = {
    val config: Config = readAndEvalConfig(runParams.args)
    val ctx = Ctx(config)
    for {
      fileOps <- paramsToFileOps(runParams)
      layoutSelector = BuildToolLayoutSelector.make[F](fileOps)
      commandExecutor = LiveCommandExecutor.make[F](layoutSelector)
      _ <- if (config.command == Repl) {
        new ReplCommand[F]().runRepl(runParams, runOnceForRepl)
      } else {
        JaddRunner.runOnce[F](ctx, fileOps, commandExecutor)
      }
    } yield ()
  }

}

object JaddRunner extends StrictLogging {
  def runOnce[F[_]: Sync](
    ctx: Ctx,
    fileOps: FileOps[F],
    commandExecutor: CommandExecutor[F]): F[Unit] = {
    val repositoryShortcuts = RepositoryShortcutsImpl
    val artifactInfoFinder: ArtifactInfoFinder =
      new ArtifactInfoFinder(
        artifactShortcuts = new ArtifactShortcuts(
          Utils.sourceFromSpringUri(ctx.config.shortcutsUri)),
        repositoryShortcuts = repositoryShortcuts
      )
    val loader = LiveLoader.make(artifactInfoFinder, repositoryShortcuts)
    commandExecutor.execute(
      ctx.config,
      loader,
      fileOps,
      () => logger.info(ctx.config.usage))
  }
}
