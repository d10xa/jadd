package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class GroovyFormatShowPrinterTest extends TestBase {
  test("testSingle") {
    GroovyFormatShowPrinter
      .single(art("a:b:1"))
      .shouldEqual("@Grab(group='a', module='b', version = '1')")
    GroovyFormatShowPrinter
      .single(art("x:y%%:2").scala2_12)
      .shouldEqual("@Grab(group='x', module='y_2.12', version = '2')")
  }
}
