package ru.d10xa.jadd.run

import cats.Parallel

import java.net.URI
import java.nio.file.Paths
import cats.syntax.all._
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import cats.effect.Resource
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.StrictLogging
import github4s.Github
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveLoader
import ru.d10xa.jadd.core.ProjectMeta
import ru.d10xa.jadd.core.ProxySettings
import ru.d10xa.jadd.coursier_.CoursierVersions
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.LiveCachedFileOps
import ru.d10xa.jadd.fs.LiveFileOps
import ru.d10xa.jadd.github.GithubFileOps
import ru.d10xa.jadd.github.GithubUrlParser
import ru.d10xa.jadd.repl.ReplCommand
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts.ArtifactShortcutsClasspath
import ru.d10xa.jadd.shortcuts.RepositoryShortcuts

class JaddRunner[F[_]: Sync: ConcurrentEffect: Parallel: ContextShift](
  cli: Cli,
  loggingUtil: LoggingUtil,
  githubUrlParser: GithubUrlParser[F],
  repositoryShortcuts: RepositoryShortcuts,
  githubResource: Resource[F, Github[F]]
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
      coursierVersions <- CoursierVersions.make[F]
      artifactInfoFinder <- ArtifactInfoFinder.make[F](repositoryShortcuts)
      artifactShortcuts = ArtifactShortcutsClasspath
      commandExecutor <- CommandExecutorImpl
        .make[F](coursierVersions, layoutSelector)
        .pure[F]
      _ <- JaddRunner
        .runOnce[F](
          ctx,
          fileOps,
          commandExecutor,
          artifactInfoFinder,
          artifactShortcuts
        )
    } yield ()

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
    commandExecutor: CommandExecutor[F],
    artifactInfoFinder: ArtifactInfoFinder[F],
    artifactShortcuts: ArtifactShortcuts
  ): F[Unit] =
    for {
      loader <- LiveLoader.make(artifactInfoFinder).pure[F]
      _ <- commandExecutor.execute(
        ctx,
        loader,
        fileOps,
        () => logger.info(ctx.config.usage),
        artifactShortcuts
      )
    } yield ()

}
