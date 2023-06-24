package ru.d10xa.jadd.code.inserts

import ru.d10xa.jadd.code.Indent
import ru.d10xa.jadd.testkit.TestBase

class IndentTest extends TestBase {
  test("take") {
    val space = " "
    Indent.space(2).take(1) shouldEqual space * 2
    Indent.space(2).take(2) shouldEqual space * 4
    Indent.space(2).take(3) shouldEqual space * 6
    Indent.space(1).take(1) shouldEqual space
    Indent.space(1).take(3) shouldEqual space * 3
  }
}
