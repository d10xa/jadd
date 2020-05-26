package ru.d10xa.jadd

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.run.RunParams
import ru.d10xa.jadd.github.LiveGithubUrlParser

object Jadd extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new JaddRunner[IO](
      cli = Cli,
      loggingUtil = LoggingUtil,
      LiveGithubUrlParser.make[IO]())
      .run(RunParams[IO](args.toVector)) *> IO(ExitCode.Success)

}
