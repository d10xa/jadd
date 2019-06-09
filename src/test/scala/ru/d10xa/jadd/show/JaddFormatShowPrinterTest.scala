package ru.d10xa.jadd.show

import ru.d10xa.jadd.show.JaddFormatShowPrinter.withVersions
import ru.d10xa.jadd.show.JaddFormatShowPrinter.withoutVersions
import ru.d10xa.jadd.testkit.TestBase

class JaddFormatShowPrinterTest extends TestBase {

  test("withVersions single") {
    withVersions.single(art("a:b:1.0")) shouldEqual "a:b:1.0"
    withVersions.single(art("a:b")) shouldEqual "a:b"
  }

  test("withoutVersions single") {
    withoutVersions.single(art("a:b:1.0")) shouldEqual "a:b"
    withoutVersions.single(art("a:b")) shouldEqual "a:b"
  }

  test("withVersions mkString") {
    val arts = List(art("a:b:1.0"), art("c:d:2.0"))
    withVersions.mkString(arts) shouldEqual "a:b:1.0\nc:d:2.0"
  }

  test("withoutVersions mkString") {
    val arts = List(art("a:b:1.0"), art("c:d:2.0"))
    withoutVersions.mkString(arts) shouldEqual "a:b\nc:d"
  }

}
