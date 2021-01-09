package ru.d10xa.jadd

import cats.MonadThrow
import cats.Parallel
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import cats.effect.ExitCode
import cats.syntax.all._
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.github.LiveGithubUrlParser
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.run.RunParams

trait Context[F[_]] {
  def run(args: List[String]): F[ExitCode]
}

object Context {

  def make[F[_]: MonadThrow: ConcurrentEffect: Parallel: ContextShift]()
    : Context[F] = new Context[F] {

    private def runner: JaddRunner[F] =
      new JaddRunner[F](
        cli = Cli,
        loggingUtil = LoggingUtil,
        LiveGithubUrlParser.make[F]()
      )

    override def run(args: List[String]): F[ExitCode] =
      runner
        .run(RunParams[F](args.toVector)) *> ExitCode.Success.pure[F]
  }
}
