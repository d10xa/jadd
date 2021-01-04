package ru.d10xa.jadd.run

import java.net.URI
import java.nio.file.Paths

import cats.syntax.all._
import cats.effect.ConcurrentEffect
import cats.effect.Resource
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.StrictLogging
import github4s.Github
import org.http4s.client.blaze.BlazeClientBuilder
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveLoader
import ru.d10xa.jadd.core.ProjectMeta
import ru.d10xa.jadd.core.ProxySettings
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.LiveCachedFileOps
import ru.d10xa.jadd.fs.LiveFileOps
import ru.d10xa.jadd.github.GithubFileOps
import ru.d10xa.jadd.github.GithubUrlParser
import ru.d10xa.jadd.repl.ReplCommand
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl
import ru.d10xa.jadd.log.LoggingUtil

import scala.concurrent.ExecutionContext

class JaddRunner[F[_]: Sync: ConcurrentEffect](
  cli: Cli,
  loggingUtil: LoggingUtil,
  githubUrlParser: GithubUrlParser[F]
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

  private def runParamsToCtx(runParams: RunParams[F]): F[Ctx] = {
    val eval: F[Config] =
      Sync[F]
        .delay(readAndEvalConfig(runParams.args))
    eval.flatMap { config =>
      if (config.projectDir.startsWith("https://github.com")) {
        githubUrlParser.parse(config.projectDir).map { urlParts =>
          Ctx(
            config,
            ProjectMeta(
              path = urlParts.file.orElse("".some),
              githubUrlParts = urlParts.some
            )
          )
        }
      } else {
        Ctx(
          config,
          ProjectMeta(path = config.projectDir.some)
        ).pure[F]
      }
    }
  }

  private def runOnce(runParams: RunParams[F]): F[Unit] =
    for {
      ctx <- runParamsToCtx(runParams)
      fileOps <- ctxToFileOps(ctx)
      layoutSelector <- BuildToolLayoutSelector.make[F](fileOps).pure[F]
      commandExecutor <- LiveCommandExecutor.make[F](layoutSelector).pure[F]
      _ <- JaddRunner.runOnce[F](ctx, fileOps, commandExecutor)
    } yield ()

  def githubResource: Resource[F, Github[F]] =
    for {
      client <- BlazeClientBuilder[F](ExecutionContext.global).resource
    } yield Github[F](client, None)

  def ctxToFileOps(ctx: Ctx): F[FileOps[F]] =
    for {
      cacheRef <- Ref.of[F, FileCache](FileCache.empty)
      ops <- ctx.meta.githubUrlParts match {
        case Some(parts) => GithubFileOps.make[F](githubResource, parts)
        case None => LiveFileOps.make(Paths.get(ctx.projectPath))
      }
      cachedOps <- LiveCachedFileOps.make(ops, cacheRef)
    } yield cachedOps

  def run(runParams: RunParams[F]): F[Unit] =
    for {
      ctx <- runParamsToCtx(runParams)
      _ <-
        if (ctx.config.command == Repl) {
          new ReplCommand[F]().runRepl(runParams, runOnce)
        } else {
          runOnce(runParams)
        }
    } yield ()

}

object JaddRunner extends StrictLogging {
  def runOnce[F[_]: Sync](
    ctx: Ctx,
    fileOps: FileOps[F],
    commandExecutor: CommandExecutor[F]
  ): F[Unit] = {
    val repositoryShortcuts = RepositoryShortcutsImpl
    val artifactInfoFinder: ArtifactInfoFinder =
      new ArtifactInfoFinder(
        artifactShortcuts = new ArtifactShortcuts(
          Utils.sourceFromSpringUri(ctx.config.shortcutsUri)
        ),
        repositoryShortcuts = repositoryShortcuts
      )
    val loader = LiveLoader.make(artifactInfoFinder, repositoryShortcuts)
    commandExecutor.execute(
      ctx,
      loader,
      fileOps,
      () => logger.info(ctx.config.usage)
    )
  }
}
