package ru.d10xa.jadd.show

import ru.d10xa.jadd.show.GradleLang.Kotlin
import ru.d10xa.jadd.testkit.TestBase

class GradleKotlinFormatShowPrinterTest extends TestBase {
  test("testSingle") {
    val result = new GradleFormatShowPrinter(Kotlin).single(art("a:b:1.0"))
    result shouldEqual "implementation (\"a:b:1.0\")"
  }
}
