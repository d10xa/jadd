package ru.d10xa.jadd.testkit

import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuiteLike
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.ScalaVersion

abstract class TestBase extends AnyFunSuiteLike with Matchers {
  implicit class ArtifactImplicits(private val artifact: Artifact) {
    def scala2_12: Artifact =
      artifact.copy(maybeScalaVersion = Some(ScalaVersion.fromString("2.12")))
    def scala2_11: Artifact =
      artifact.copy(maybeScalaVersion = Some(ScalaVersion.fromString("2.11")))
  }
  def art(s: String): Artifact = Artifact.fromString(s).toOption.get
}
