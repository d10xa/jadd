package ru.d10xa.jadd

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object Jadd extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Context.make[IO]().run(args)

}
