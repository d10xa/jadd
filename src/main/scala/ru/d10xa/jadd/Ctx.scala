package ru.d10xa.jadd

import ru.d10xa.jadd.Cli.Config

final case class Ctx(
  config: Config
)

object Ctx {
  lazy val version: String =
    Option(Jadd.getClass.getPackage.getImplementationVersion)
      .getOrElse("SNAPSHOT")
}
