package ru.d10xa.jadd.view

import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.SbtArtifactMatcher
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.view.ArtifactView.Match
import ru.d10xa.jadd.view.ArtifactView.MatchImpl

class ArtifactViewTest extends TestBase {

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

  def find(artifact: Artifact, source: String): Seq[Match] =
    new SbtArtifactMatcher(source).find(artifact)

  test("sbt find artifact") {
    val artifact = Artifact(
      GroupId("com.typesafe.scala-logging"),
      "scala-logging%%",
      maybeScalaVersion = Some(ScalaVersion.fromString("2.12"))
    )
    find(artifact, sbtSource).head.value should be(
      """libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0""""
    )
  }

  test("sbt find artifact with Test configuration") {
    find(art("ch.qos.logback:logback-classic"), sbtSource).head.value should be(
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

    val matches = find(art("ch.qos.logback:logback-classic"), sbtSource)

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
      MatchImpl(start = source.indexOf("dependency1"), value = "dependency1")
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
