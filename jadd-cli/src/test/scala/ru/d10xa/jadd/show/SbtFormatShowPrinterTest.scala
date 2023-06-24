package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class SbtFormatShowPrinterTest extends TestBase {
  test("testSingle") {
    SbtFormatShowPrinter
      .single(art("a:b:1"))
      .shouldEqual("libraryDependencies += \"a\" % \"b\" % \"1\"")
    SbtFormatShowPrinter
      .single(art("x:y%%:2").scala2_12)
      .shouldEqual("libraryDependencies += \"x\" %% \"y\" % \"2\"")
  }
}
