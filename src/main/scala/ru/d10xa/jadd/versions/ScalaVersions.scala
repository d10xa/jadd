package ru.d10xa.jadd.versions

import cats.data.NonEmptyList

object ScalaVersions {
  val supportedMinorVersions: NonEmptyList[String] =
    NonEmptyList.of("2.13", "2.12", "2.11")
  val defaultScalaVersion: String = "2.12"
}
