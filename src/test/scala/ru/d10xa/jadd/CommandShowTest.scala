package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.show.SbtShowCommand

class CommandShowTest extends FunSuite with Matchers {

  test("sbt") {
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
    """"ch.qos.logback" % "logback-classic" % "1.2.3"
      |"com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
      |"io.circe" %% "circe-parser" % "0.9.3"
      |"io.circe" %% "circe-generic" % "0.9.3"
      |"org.jline" % "jline" % "3.7.1"""".stripMargin
  }

}
