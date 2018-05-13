package ru.d10xa.jadd.inserts

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact

class SbtFileInsertsTest extends FunSuiteLike with Matchers {

  def add(content: String, artifact: Artifact): String = new SbtFileInserts().append(content, Seq(artifact))

  test("sbt insert dependency successfully") {
    val content =
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |""".stripMargin

    val result = new SbtFileInserts().append(
      content,
      Seq(Artifact(groupId = "ch.qos.logback", artifactId = "logback-classic", maybeVersion = Some("1.2.3")))
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

    val result = new SbtFileInserts().append(
      content,
      List(
        Artifact(
          groupId = "org.typelevel",
          artifactId = "cats-core%%",
          shortcut = Some("cats-core"),
          maybeVersion = Some("1.1.0"),
          maybeScalaVersion = Some("2.12")
        )
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

    val result = new SbtFileInserts().append(
      content,
      List(
        Artifact(
          groupId = "org.typelevel",
          artifactId = "cats-core%%",
          maybeVersion = Some("1.1.0"),
          maybeScalaVersion = Some("2.12")
        )
      )
    )

    result shouldEqual
      """import Dependencies._
        |libraryDependencies += scalaTest % Test
        |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin
  }

  test("update dependency version with explicit scala version") {
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

    add(content, artifact.copy(explicitScalaVersion = true)) shouldEqual
      """
        |libraryDependencies += "org.typelevel" % "cats-core_2.11" % "1.1.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin

    add(content, artifact.copy(explicitScalaVersion = false)) shouldEqual
      """
        |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin
  }

}
