package ru.d10xa.jadd.versions

import cats.data.NonEmptyList
import ru.d10xa.jadd.core.types.ScalaVersion

object ScalaVersions {
  val supportedMinorVersions: NonEmptyList[ScalaVersion] =
    NonEmptyList.of("2.13", "2.12", "2.11").map(ScalaVersion.fromString)
  val defaultScalaVersion: ScalaVersion = ScalaVersion.fromString("2.12")
}
