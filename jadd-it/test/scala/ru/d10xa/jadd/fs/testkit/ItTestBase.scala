package ru.d10xa.jadd.fs.testkit

import cats.effect.unsafe.IORuntime
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

abstract class ItTestBase extends AnyFunSuiteLike with Matchers {
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
}
