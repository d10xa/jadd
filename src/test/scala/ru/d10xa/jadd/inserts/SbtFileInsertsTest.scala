package ru.d10xa.jadd.inserts

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class SbtFileInsertsTest extends FlatSpec with Matchers {

  "sbt adder" should "successfully add dependency" in {
    val content =
      """import Dependencies._
        |libraryDependencies += scalaTest % Test""".stripMargin

    val result = new SbtFileInserts().append(
      content.split("/n").toList,
      List(
        "libraryDependencies += \"ch.qos.logback\" % \"logback-classic\" % \"1.2.3\""
      )
    )
    result.mkString("\n") shouldEqual
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"""".stripMargin
  }
}
