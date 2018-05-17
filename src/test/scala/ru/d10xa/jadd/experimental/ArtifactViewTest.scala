package ru.d10xa.jadd.experimental

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.view.ArtifactView.Match

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
    SbtPipeline.sbtArtifactView.find(artifact, sbtSource).head.value should be(
      """libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0""""
    )
  }

  test("sbt find artifact with Test configuration") {
    SbtPipeline.sbtArtifactView.find(Artifact("ch.qos.logback", "logback-classic"), sbtSource).head.value should be(
      """libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test"""
    )
  }

  test("find duplicates") {
    val sbtSource: String =
      """
        |libraryDependencies += "io.circe" % "circe-parser_2.12" % "0.9.2"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
        |libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"
        |libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
      """.stripMargin

    val matches = SbtPipeline.sbtArtifactView.find(Artifact("ch.qos.logback", "logback-classic"), sbtSource)

    matches.size shouldEqual 2

    val Seq(m1, m2) = matches

    m1.value shouldEqual """libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test"""
    m2.value shouldEqual """libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test"""
    m1.start shouldEqual 67
    m2.start shouldEqual 229
  }

  test("replace match") {
    val source =
      """
        |libraryDependencies ++= Seq(
        |  dependency1,
        |  dependency2
        |)
      """.stripMargin

    val result =
      Match(start = source.indexOf("dependency1"), value = "dependency1")
      .replace(source, "dep42")

    result shouldEqual
      """
        |libraryDependencies ++= Seq(
        |  dep42,
        |  dependency2
        |)
      """.stripMargin
  }

}
