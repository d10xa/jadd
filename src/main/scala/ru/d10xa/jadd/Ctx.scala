package ru.d10xa.jadd

import ru.d10xa.jadd.cli.Config

final case class Ctx(
  config: Config,
  meta: ProjectMeta = ProjectMeta(scalaVersion = None)
)

object Ctx {
  lazy val version: String =
    Option(Jadd.getClass.getPackage.getImplementationVersion)
      .getOrElse("SNAPSHOT")
}
