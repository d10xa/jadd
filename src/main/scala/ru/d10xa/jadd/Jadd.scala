package ru.d10xa.jadd

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.run.LiveCommandExecutor
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.run.RunParams
import cats.implicits._

object Jadd extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val commandExecutor: LiveCommandExecutor[IO] = new LiveCommandExecutor[IO]
    new JaddRunner[IO](cli = Cli, loggingUtil = LoggingUtil)
      .run(RunParams[IO](args.toVector, commandExecutor)) *> IO(
      ExitCode.Success)
  }

}
