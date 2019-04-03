package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class GroovyFormatShowPrinterTest extends TestBase {
  test("testMkString") {
    val str = GroovyFormatShowPrinter.mkString(
      List(
        art("a:b:1"),
        art("x:y%%:2").scala2_12,
      ))
    val expected =
      """@Grab(group='a', module='b', version = '1')
        |@Grab(group='x', module='y_2.12', version = '2')""".stripMargin
    str shouldEqual expected
  }

}
