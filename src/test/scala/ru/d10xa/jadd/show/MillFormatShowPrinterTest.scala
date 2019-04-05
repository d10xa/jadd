package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class MillFormatShowPrinterTest extends TestBase {
  test("testMkString") {
    MillFormatShowPrinter
      .mkString(List(art("a:b:1"), art("x:y%%:2").scala2_12))
      .shouldEqual("""ivy"a:b:1",
                     |ivy"x::y:2"""".stripMargin)
  }

}
