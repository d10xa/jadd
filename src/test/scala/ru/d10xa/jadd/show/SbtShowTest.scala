package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.ProjectFileReaderMemory

class SbtShowTest extends TestBase {

  val emptyProjectFileReader = new ProjectFileReaderMemory(Map.empty)

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

    val result = new SbtShowCommand(source, emptyProjectFileReader).show()

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

    val result = new SbtShowCommand(source, emptyProjectFileReader).show()

    result shouldEqual
      """|com.typesafe.scala-logging:scala-logging_2.12:3.9.0
         |org.typelevel:cats-core_2.12:1.1.0""".stripMargin
  }

  test("Dependencies import") {
    val source =
      s"""
         |import Dependencies._
         |libraryDependencies ++= Seq(
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
         |)
       """.stripMargin

    val dependenciesFile =
      """
        |import sbt._
        |object Dependencies {
        |  lazy val junit = "junit" % "junit" % "4.12"
        |  lazy val catsCore = "org.typelevel" %% "cats-core" % "1.1.0"
        |}""".stripMargin
    val result = new SbtShowCommand(
      source,
      new ProjectFileReaderMemory(
        Map("project/Dependencies.scala" ->
          dependenciesFile))
    ).show()

    result shouldEqual
      """|com.typesafe.scala-logging:scala-logging_2.12:3.9.0
         |junit:junit:4.12
         |org.typelevel:cats-core_2.12:1.1.0""".stripMargin
  }

}
