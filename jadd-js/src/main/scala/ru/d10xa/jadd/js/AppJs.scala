package ru.d10xa.jadd.js

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import ru.d10xa.jadd.core.types.ScalaVersion

import scala.scalajs.js.annotation.JSExportTopLevel

object AppJs extends IOApp {

  @JSExportTopLevel("render_jadd_app")
  final def mainJs(): Unit = main(Array.empty)

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO.println(ScalaVersion.fromString("2.11"))
    } yield ExitCode.Success
}
