package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.testkit.TestBase

class SbtFileInsertsTest extends TestBase {

  def add(content: String, artifacts: Artifact*): String =
    new SbtFileInserts().appendAll(content, artifacts)

  test("sbt insert dependency successfully") {
    val content =
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |""".stripMargin

    val result = add(
      content,
      Artifact(groupId = "ch.qos.logback", artifactId = "logback-classic", maybeVersion = Some("1.2.3"))
    )

    result.trim shouldEqual
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin.trim
  }

  test("add dependency to sbt and resolve scala version") {
    val content =
      """import Dependencies._
        |libraryDependencies += scalaTest % Test""".stripMargin

    val result = add(
      content,
      Artifact(
        groupId = "org.typelevel",
        artifactId = "cats-core%%",
        shortcut = Some("cats-core"),
        maybeVersion = Some("1.1.0"),
        maybeScalaVersion = Some("2.12")
      )
    )

    result shouldEqual
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
        |""".stripMargin
  }

  test("update dependency version with implicit scala version") {
    val content =
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin

    val result = add(
      content,
      Artifact(
        groupId = "org.typelevel",
        artifactId = "cats-core%%",
        maybeVersion = Some("1.1.0"),
        maybeScalaVersion = Some("2.12")
      )
    )

    result shouldEqual
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin
  }

  test("update explicit scala version to explicit") {
    val content =
      """
        |libraryDependencies += "org.typelevel" % "cats-core_2.11" % "1.0.1"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin

    val artifact = Artifact(
      groupId = "org.typelevel",
      artifactId = "cats-core%%",
      maybeVersion = Some("1.1.0"),
      maybeScalaVersion = Some("2.11")
    )

    val result = add(content, artifact.copy(explicitScalaVersion = true))
    result shouldEqual
      """
        |libraryDependencies += "org.typelevel" % "cats-core_2.11" % "1.1.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin

  }

  test("update explicit scala version to implicit") {
    val content =
      """
        |libraryDependencies += "org.typelevel" % "cats-core_2.11" % "1.0.1"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin

    val artifact = Artifact(
      groupId = "org.typelevel",
      artifactId = "cats-core%%",
      maybeVersion = Some("1.1.0"),
      maybeScalaVersion = Some("2.11")
    )

    val result = add(content, artifact.copy(explicitScalaVersion = false))

    result shouldEqual
      """
        |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin
  }

  test("sbt libraryDependencies add sequence") {
    // TODO add comment before sequence //libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"
    val content =
      s"""
         |libraryDependencies ++= Seq(
         |  "ch.qos.logback" % "logback-classic" % "1.2.3",
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
         |  "io.circe" %% "circe-parser" % "0.9.3"
         |)
         |
         |//libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"
         |""".stripMargin

    val artifact = Artifact(
      groupId = "com.typesafe.scala-logging",
      artifactId = "scala-logging%%",
      maybeVersion = Some("3.9.0"),
      maybeScalaVersion = Some("2.12"),
      inSequence = true
    )

    val result = add(content, artifact)

    result shouldEqual
      s"""
         |libraryDependencies ++= Seq(
         |  "ch.qos.logback" % "logback-classic" % "1.2.3",
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
         |  "io.circe" %% "circe-parser" % "0.9.3"
         |)
         |
         |//libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"
         |""".stripMargin

  }

    test("update seq and standalone") {
      val content =
        s"""
           |libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
           |libraryDependencies ++= Seq(
           |  "ch.qos.logback" % "logback-classic" % "1.2.2"
           |)
           |""".stripMargin

      val a1 = Artifact(
        groupId = "com.typesafe.scala-logging",
        artifactId = "scala-logging%%",
        maybeVersion = Some("3.9.0"),
        maybeScalaVersion = Some("2.12")
      )

      val a2 = Artifact(
        groupId = "ch.qos.logback",
        artifactId = "logback-classic",
        maybeVersion = Some("1.2.3"),
        inSequence = true
      )

      add(content, a1, a2) shouldEqual
        s"""
           |libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
           |libraryDependencies ++= Seq(
           |  "ch.qos.logback" % "logback-classic" % "1.2.3"
           |)
           |""".stripMargin

    }

}
