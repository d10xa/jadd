package ru.d10xa.jadd.show

import cats.effect.SyncIO
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.code.SbtFileUtils
import ru.d10xa.jadd.code.scalameta.SbtArtifactsParser
import ru.d10xa.jadd.code.scalameta.SbtModuleParser
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.LiveSbtScalaVersionFinder
import ru.d10xa.jadd.core.Scope.Test
import ru.d10xa.jadd.testkit.TestBase

class SbtShowCommandTest extends TestBase {

  private val sbtModuleParser: SbtModuleParser[SyncIO] =
    SbtModuleParser
      .make[SyncIO]()
      .unsafeRunSync()

  private val sbtArtifactsParser: SbtArtifactsParser[SyncIO] =
    SbtArtifactsParser
      .make[SyncIO](sbtModuleParser)
      .unsafeRunSync()

  def showArtifacts(files: (String, String)*): List[Artifact] =
    tempFileOpsResource[SyncIO](files: _*)
      .use { case (_, fileOps) =>
        val scalaVersions =
          LiveSbtScalaVersionFinder
            .make[SyncIO](Ctx(Config.empty), fileOps)
        new SbtShowCommand[SyncIO](
          SbtFileUtils.make[SyncIO](fileOps).unsafeRunSync(),
          fileOps,
          scalaVersions,
          sbtArtifactsParser
        )
          .show()
          .map(_.toList)
      }
      .unsafeRunSync()

  test("single java dependency") {
    val source =
      s"""
         |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.6"
       """.stripMargin
    val result = showArtifacts("build.sbt" -> source)
    result.head shouldBe art("ch.qos.logback:logback-classic:1.2.6")
  }

  test("single scala dependency") {
    val source =
      s"""
         |"io.circe" %% "circe-parser" % "0.9.3"
       """.stripMargin
    val result = showArtifacts("build.sbt" -> source)
    result.head shouldBe art("io.circe:circe-parser%%:0.9.3").scala2_12
  }

  test("seq") {
    val source =
      s"""
         |libraryDependencies ++= Seq(
         |  "ch.qos.logback" % "logback-classic" % "1.2.6",
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
         |  "io.circe" %% "circe-parser" % "0.9.3",
         |  "io.circe" %% "circe-generic" % "0.9.3",
         |  "org.jline" % "jline" % "3.7.1"
         |)
       """.stripMargin

    val result = showArtifacts("build.sbt" -> source)

    val expected = Seq(
      art("ch.qos.logback:logback-classic:1.2.6"),
      art("com.typesafe.scala-logging:scala-logging%%:3.9.0").scala2_12,
      art("io.circe:circe-parser%%:0.9.3").scala2_12,
      art("io.circe:circe-generic%%:0.9.3").scala2_12,
      art("org.jline:jline:3.7.1")
    )

    (result should contain).theSameElementsAs(expected)
  }

  test("seq and single") {
    val source =
      s"""
         |libraryDependencies ++= Seq(
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
         |)
         |libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"
       """.stripMargin

    val result = showArtifacts("build.sbt" -> source)

    val expected = Seq(
      art("com.typesafe.scala-logging:scala-logging%%:3.9.0").scala2_12,
      art("org.typelevel:cats-core%%:1.1.0").scala2_12
    )
    (result should contain).theSameElementsAs(expected)
  }

  test("with scope test") {
    val source =
      s"""
         |libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
         |libraryDependencies ++= Vector("junit" % "junit" % "4.12" % Test)
         |libraryDependencies += "a" % "b" % "1" % UnknownScopeShouldBeIgnored
       """.stripMargin

    val result: List[Artifact] = showArtifacts("build.sbt" -> source)

    val expected: List[Artifact] = List(
      art("org.scalatest:scalatest_2.12:3.0.5")
        .copy(scope = Some(Test))
        .scala2_12,
      art("junit:junit:4.12")
        .copy(scope = Some(Test)),
      art("a:b:1")
    )
    (result should contain).theSameElementsAs(expected)
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
    val result =
      showArtifacts(
        "build.sbt" -> source,
        "project/Dependencies.scala" -> dependenciesFile
      )

    val expected = Seq(
      art("com.typesafe.scala-logging:scala-logging%%:3.9.0").scala2_12,
      art("junit:junit:4.12"),
      art("org.typelevel:cats-core%%:1.1.0").scala2_12
    )
    (result should contain).theSameElementsAs(expected)
  }

  test("build.sbt has defined scala version 2.11") {
    val source =
      s"""
         |// scalaVersion in ThisBuild := "2.11.0"
         |libraryDependencies ++= Seq(
         |  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
         |)
       """.stripMargin

    val result = showArtifacts("build.sbt" -> source)

    val expected = Seq(
      art("com.typesafe.scala-logging:scala-logging%%:3.9.0").scala2_11
    )
    (result should contain).theSameElementsAs(expected)
  }

  test("version from val") {
    val source =
      s"""
         |val someVersion = "3.9.0"
         |libraryDependencies ++= Seq(
         |  "a" % "b" % someVersion
         |)
       """.stripMargin

    val result = showArtifacts("build.sbt" -> source)
    val expected = Seq(
      art("a:b:3.9.0")
    )
    (result should contain).theSameElementsAs(expected)
  }

  test("inner object") {
    val source =
      s"""
         |val versions = new {
         |  val x = new {
         |    val v = "1"
         |  }
         |}
         |libraryDependencies += "a" % "b" % versions.x.v
       """.stripMargin

    val result = showArtifacts("build.sbt" -> source)
    val expected = Seq(art("a:b:1"))
    (result should contain).theSameElementsAs(expected)
  }

}
