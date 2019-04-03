package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class GradleFormatShowPrinterTest extends TestBase {
  test("testSingle") {
    val result = GradleFormatShowPrinter.single(art("a:b:1.0"))
    result shouldEqual "implementation 'a:b:1.0'"
  }

}
