package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class MavenFormatShowPrinterTest extends TestBase {
  test("testSingle") {
    val result = MavenFormatShowPrinter.single(art("a:b:1.0"))
    val nl = "\n    "
    result shouldEqual s"<dependency>" +
      s"$nl<groupId>a</groupId>" +
      s"$nl<artifactId>b</artifactId>" +
      s"$nl<version>1.0</version>\n</dependency>"
  }
}
