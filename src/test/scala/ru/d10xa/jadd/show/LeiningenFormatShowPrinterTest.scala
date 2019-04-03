package ru.d10xa.jadd.show

import ru.d10xa.jadd.show.LeiningenFormatShowPrinter.single
import ru.d10xa.jadd.testkit.TestBase

class LeiningenFormatShowPrinterTest extends TestBase {

  test("testSingle") {
    single(art("a:b:1")) shouldEqual """[a/b "1"]"""
    single(art("a:a:2.0.0-RC1")) shouldEqual """[a "2.0.0-RC1"]"""
  }

}
