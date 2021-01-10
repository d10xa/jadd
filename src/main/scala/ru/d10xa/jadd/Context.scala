package ru.d10xa.jadd

import cats.MonadThrow
import cats.Parallel
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import cats.effect.ExitCode
import cats.effect.Resource
import cats.syntax.all._
import github4s.Github
import org.http4s.client.blaze.BlazeClientBuilder
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.github.LiveGithubUrlParser
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.run.RunParams
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl

import scala.concurrent.ExecutionContext

trait Context[F[_]] {
  def run(args: List[String]): F[ExitCode]
}

object Context {

  def make[F[_]: MonadThrow: ConcurrentEffect: Parallel: ContextShift]()
    : Context[F] = new Context[F] {

    private def githubResource: Resource[F, Github[F]] =
      for {
        client <- BlazeClientBuilder[F](ExecutionContext.global).resource
      } yield Github[F](client, accessToken = None)

    private def runner: JaddRunner[F] =
      new JaddRunner[F](
        cli = Cli,
        loggingUtil = LoggingUtil,
        LiveGithubUrlParser.make[F](),
        RepositoryShortcutsImpl,
        githubResource
      )

    override def run(args: List[String]): F[ExitCode] =
      runner
        .run(RunParams[F](args.toVector)) *> ExitCode.Success.pure[F]
  }
}
