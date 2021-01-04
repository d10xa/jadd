package ru.d10xa.jadd.code.inserts

import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.view.ArtifactView
import ru.d10xa.jadd.view.ArtifactView.MatchImpl

class SbtArtifactMatcherTest extends TestBase {

  private val scalaLogging =
    Artifact(GroupId("com.typesafe.scala-logging"), "scala-logging%%")
  private val logbackClassic =
    Artifact(GroupId("ch.qos.logback"), "logback-classic")

  test("find in sequence and standalone in single source") {
    val source =
      """
        |libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
        |libraryDependencies ++= Seq(
        |  "ch.qos.logback" % "logback-classic" % "1.2.2",
        |)
        |// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
      """.stripMargin
    val matcher = new SbtArtifactMatcher(source)

    val inSeqMatches: Seq[ArtifactView.Match] =
      matcher.findInSequence(logbackClassic)
    inSeqMatches.size shouldEqual 1
    inSeqMatches.head.value shouldEqual """"ch.qos.logback" % "logback-classic" % "1.2.2""""
    inSeqMatches.head.start shouldEqual 113
    inSeqMatches.head.inSequence shouldEqual true

    val standaloneMatches = matcher.findStandalone(scalaLogging)
    standaloneMatches.size shouldEqual 1
    standaloneMatches.head.value shouldEqual """libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0""""
    standaloneMatches.head.start shouldEqual 1
    standaloneMatches.head.inSequence shouldEqual false
  }

  test("ignore commented matches") {
    val source =
      """
        |// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
        |libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
      """.stripMargin
    val matcher = new SbtArtifactMatcher(source)
    val matches = matcher.findStandalone(scalaLogging)
    matches.size shouldEqual 1
    matches.head.start shouldEqual 85
  }

  test("isCommented") {
    val source =
      """
        |// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
        |libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
      """.stripMargin
    val matcher = new SbtArtifactMatcher(source)

    matcher
      .isCommented(
        MatchImpl(
          start = 4,
          value =
            """libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0""""
        )
      )
      .shouldEqual(true)

    matcher
      .isCommented(
        MatchImpl(
          start = 85,
          value =
            """libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0""""
        )
      )
      .shouldEqual(false)
  }

}
