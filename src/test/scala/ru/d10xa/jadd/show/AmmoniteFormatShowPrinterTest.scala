package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class AmmoniteFormatShowPrinterTest extends TestBase {
  test("testMkString") {
    val str = AmmoniteFormatShowPrinter.mkString(
      List(
        art("ch.qos.logback:logback-classic:1.2.6"),
        art("com.typesafe.scala-logging:scala-logging%%:3.9.0").scala2_12
      )
    )
    str shouldEqual """import $ivy.`ch.qos.logback:logback-classic:1.2.6`
                      |import $ivy.`com.typesafe.scala-logging::scala-logging:3.9.0`""".stripMargin
  }
}
