package ru.d10xa.jadd.show

import org.scalatest.FunSuite
import org.scalatest.Matchers

class SbtShowTest extends FunSuite with Matchers {

  test("seq") {
    val source =
      s"""
         |libraryDependencies ++= Seq(
         |  "ch.qos.logback" % "logback-classic" % "1.2.3",
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
         |  "io.circe" %% "circe-parser" % "0.9.3",
         |  "io.circe" %% "circe-generic" % "0.9.3",
         |  "org.jline" % "jline" % "3.7.1"
         |)
       """.stripMargin

    val result = new SbtShowCommand(source).show()

    result shouldEqual
      """|ch.qos.logback:logback-classic:1.2.3
       |com.typesafe.scala-logging:scala-logging_2.12:3.9.0
       |io.circe:circe-parser_2.12:0.9.3
       |io.circe:circe-generic_2.12:0.9.3
       |org.jline:jline:3.7.1""".stripMargin
  }

  test("seq and single") {
    val source =
      s"""
         |libraryDependencies ++= Seq(
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
         |)
         |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
       """.stripMargin

    val result = new SbtShowCommand(source).show()

    result shouldEqual
      """|com.typesafe.scala-logging:scala-logging_2.12:3.9.0
         |org.typelevel:cats-core_2.12:1.1.0""".stripMargin
  }

}
