package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class AmmoniteFormatShowPrinterTest extends TestBase {
  test("testMkString") {
    val str = AmmoniteFormatShowPrinter.mkString(
      List(
        art("a.b.c:d-e:1.2.3"),
        art("x.y:z%%:4.5.6").scala2_12
      )
    )
    str shouldEqual """import $ivy.`a.b.c:d-e:1.2.3`
                      |import $ivy.`x.y::z:4.5.6`""".stripMargin
  }
}
