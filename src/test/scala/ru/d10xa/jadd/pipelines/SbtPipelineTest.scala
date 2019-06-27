package ru.d10xa.jadd.pipelines

import ru.d10xa.jadd.testkit.TestBase

class SbtPipelineTest extends TestBase {

  test("testExtractScalaVersionFromBuildSbt scalaVersion in ThisBuild") {
    val Some(scalaVersion) = SbtPipeline.extractScalaVersionFromBuildSbt(
      """
        |organization in ThisBuild := "com.example"
        |scalaVersion in ThisBuild := "2.12.8"
        |version in ThisBuild := "0.0.0"
        |
      """.stripMargin)
    scalaVersion shouldEqual "2.12"
  }

  test("testExtractScalaVersionFromBuildSbt scalaVersion") {
    val Some(scalaVersion) = SbtPipeline.extractScalaVersionFromBuildSbt(
      """
        |lazy val commonSettings = Seq(
        |  organization := "com.example",
        |  name := "example-name",
        |  scalaVersion := "2.13.0",
        |  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0"),
        |}
      """.stripMargin)
    scalaVersion shouldEqual "2.13"
  }

  test("testExtractScalaVersionFromBuildSbt scalaVersion in comment") {
    val Some(scalaVersion) =
      SbtPipeline.extractScalaVersionFromBuildSbt(
        """
        |//  scalaVersion := "2.11.12"
        |scalaVersion in ThisBuild := "2.12.8"
        |}
      """.stripMargin)
    scalaVersion shouldEqual "2.11"
  }

}
