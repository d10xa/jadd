package ru.d10xa.jadd.pipelines

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Cli.Config
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder

class SbtPipelineTest extends FunSuite with Matchers {

  test("add dependency to sbt and resolve scala version"){
    val content =
      """import Dependencies._
        |libraryDependencies += scalaTest % Test""".stripMargin

    val result = new SbtPipeline(Ctx(Config()))(new ArtifactInfoFinder()).makeNewContent(
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
        |libraryDependencies += "org.typelevel" % "cats-core_2.12" % "1.1.0"
        |""".stripMargin

  }
}
