package ru.d10xa.jadd

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.run.RunParams
import cats.implicits._
import github4s.Github
import ru.d10xa.jadd.github.LiveGithubUrlParser

object Jadd extends IOApp {

  import scala.concurrent.ExecutionContext.Implicits.global
  val github: Github[IO] = Github[IO](None, None)

  override def run(args: List[String]): IO[ExitCode] =
    new JaddRunner[IO](
      cli = Cli,
      loggingUtil = LoggingUtil,
      github,
      LiveGithubUrlParser.make[IO]())
      .run(RunParams[IO](args.toVector)) *> IO(ExitCode.Success)

}
