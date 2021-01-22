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
import github4s.Github
import ru.d10xa.jadd.application.CliArgs
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
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.repl.ReplCommand
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts.ArtifactShortcutsClasspath
import ru.d10xa.jadd.shortcuts.RepositoryShortcuts

class JaddRunner[F[_]: Sync: ConcurrentEffect: Parallel: ContextShift](
  cli: Cli,
  githubUrlParser: GithubUrlParser[F],
  repositoryShortcuts: RepositoryShortcuts,
  githubResource: Resource[F, Github[F]]
) {

  private def readConfig(args: Vector[String]): Config = cli.parse(args)

  private def readAndEvalConfig(config: Config): (Config, Logger[F]) = {
    def initProxy(proxyStr: String): Unit = {
      val proxyUri = new URI(proxyStr)
      val proxySettings = ProxySettings.fromURI(proxyUri)
      ProxySettings.set(proxySettings)
      (proxySettings.httpProxyUser, proxySettings.httpProxyPassword) match {
        case (Some(u), Some(p)) => ProxySettings.setupAuthenticator(u, p)
        case _ => // do nothing
      }
    }
    config.proxy.foreach(initProxy)
    (
      config,
      Logger.make[F](
        debug = config.debug,
        quiet = config.quiet
      )
    )
  }

  private def dynamicContext(cliArgs: CliArgs): F[(Config, Logger[F])] =
    Sync[F]
      .delay(readConfig(cliArgs.args))
      .map(readAndEvalConfig)

  private def parseProjectMeta(config: Config): F[ProjectMeta] =
    if (config.projectDir.startsWith("https://github.com")) {
      githubUrlParser.parse(config.projectDir).map { urlParts =>
        ProjectMeta(
          path = urlParts.file.orElse("".some),
          githubUrlParts = urlParts.some
        )
      }
    } else {
      ProjectMeta(path = config.projectDir.some).pure[F]
    }

  private def runParamsToCtx(cliArgs: CliArgs): F[(Ctx, Logger[F])] =
    for {
      cl <- dynamicContext(cliArgs)
      config = cl._1
      logger = cl._2
      projectMeta <- parseProjectMeta(config)
    } yield (Ctx(config, projectMeta), logger)

  private def runOnce(cliArgs: CliArgs): F[Unit] =
    for {
      ctxAndLogger <- runParamsToCtx(cliArgs)
      ctx = ctxAndLogger._1
      logger = ctxAndLogger._2
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
          artifactShortcuts,
          logger
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

  def run(cliArgs: CliArgs): F[Unit] =
    for {
      ctxAndLogger <- runParamsToCtx(cliArgs)
      ctx = ctxAndLogger._1
      _ <-
        if (ctx.config.command == Repl) {
          implicit val logger: Logger[F] = ctxAndLogger._2
          new ReplCommand[F]().runRepl(cliArgs, runOnce)
        } else {
          runOnce(cliArgs)
        }
    } yield ()

}

object JaddRunner {
  def runOnce[F[_]: Sync](
    ctx: Ctx,
    fileOps: FileOps[F],
    commandExecutor: CommandExecutor[F],
    artifactInfoFinder: ArtifactInfoFinder[F],
    artifactShortcuts: ArtifactShortcuts,
    logger: Logger[F]
  ): F[Unit] =
    for {
      loader <- LiveLoader.make(artifactInfoFinder).pure[F]
      _ <- commandExecutor.execute(
        ctx,
        loader,
        fileOps,
        () => logger.info(ctx.config.usage),
        artifactShortcuts
      )(logger)
    } yield ()

}
