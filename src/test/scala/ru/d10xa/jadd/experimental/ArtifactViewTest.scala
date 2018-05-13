package ru.d10xa.jadd.experimental

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.pipelines.SbtPipeline

class ArtifactViewTest extends FunSuiteLike with Matchers {

  val sbtSource: String =
    """
      |enablePlugins(JavaAppPackaging)
      |
      |libraryDependencies += scalaXml
      |libraryDependencies += scopt
      |libraryDependencies += scalaTest % Test
      |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
      |libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"
      |libraryDependencies += "io.circe" % "circe-parser_2.12" % "0.9.2"
      |
      """.stripMargin

  test("sbt find artifact") {
    val artifact = Artifact("com.typesafe.scala-logging", "scala-logging%%", maybeScalaVersion = Some("2.12"))
    SbtPipeline.sbtArtifactView.find(artifact, sbtSource).get should be(
      """libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0""""
    )
  }

  test("sbt find artifact with Test configuration") {
    SbtPipeline.sbtArtifactView.find(Artifact("ch.qos.logback", "logback-classic"), sbtSource).get should be(
      """libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test"""
    )
  }

}
