package ru.d10xa.jadd.code

import scala.meta.Source
import scala.meta.dialects
import org.scalatest.Assertions.fail

package object scalameta {
  def parseSource(str: String): Source =
    dialects
      .Sbt1(str)
      .parse[Source]
      .toEither match {
      case Right(v) => v
      case Left(_) => fail("can not parse")
    }
}
