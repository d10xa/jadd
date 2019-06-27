package ru.d10xa.jadd.testkit

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact

abstract class TestBase extends FunSuite with Matchers {
  implicit class ArtifactImplicits(private val artifact: Artifact) {
    def scala2_12: Artifact = artifact.copy(maybeScalaVersion = Some("2.12"))
    def scala2_11: Artifact = artifact.copy(maybeScalaVersion = Some("2.11"))
  }
  def art(s: String): Artifact = Artifact.fromString(s).right.get
}
