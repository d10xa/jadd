package ru.d10xa.jadd.testkit

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact

abstract class TestBase extends FunSuite with Matchers {
  def art(s: String): Artifact = Artifact.fromString(s).right.get
}
