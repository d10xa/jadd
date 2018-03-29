package ru.d10xa.jadd.inserts

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Indent

class IndentTest extends FunSuite with Matchers {
  test("take") {
    val space = " "
    Indent.space(2).take() shouldEqual space * 2
    Indent.space(2).take(1) shouldEqual space * 2
    Indent.space(2).take(2) shouldEqual space * 4
    Indent.space(2).take(3) shouldEqual space * 6
    Indent.space(1).take(1) shouldEqual space
    Indent.space(1).take(3) shouldEqual space * 3
  }
}
