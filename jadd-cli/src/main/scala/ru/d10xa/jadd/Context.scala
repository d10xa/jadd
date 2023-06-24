package ru.d10xa.jadd

import cats.MonadThrow
import cats.Parallel
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.Resource
import cats.syntax.all._
import github4s.Github
import org.http4s.blaze.client.BlazeClientBuilder
import ru.d10xa.jadd.application.CliArgs
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.github.LiveGithubUrlParser
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl

trait Context[F[_]] {
  def run(args: List[String]): F[ExitCode]
}

object Context {

  def make[F[_]: MonadThrow: Async: Parallel](): Context[F] = new Context[F] {

    private def githubResource: Resource[F, Github[F]] =
      for {
        client <- BlazeClientBuilder[F].resource
      } yield Github[F](client, accessToken = None)

    private def runner: JaddRunner[F] =
      new JaddRunner[F](
        cli = Cli,
        LiveGithubUrlParser.make[F](),
        RepositoryShortcutsImpl,
        githubResource
      )

    override def run(args: List[String]): F[ExitCode] =
      runner
        .run(CliArgs(args.toVector)) *> ExitCode.Success.pure[F]
  }
}
