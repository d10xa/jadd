package ru.d10xa.jadd.code.scalameta

import ru.d10xa.jadd.testkit.TestBase

import scala.meta.Term

class ScalametaTest extends TestBase {
  test("GroupIdPercentArtifactIdMatch.unapply") {
    parseSource("\"org.jline\" % \"jline\"").stats.head match {
      case GroupIdPercentArtifactIdMatch(
          GroupIdPercentArtifactId(groupId, artifactId, percents)) =>
        groupId shouldBe "org.jline"
        artifactId shouldBe "jline"
        percents.n shouldBe 1
    }
  }

  test("PercentCharsMatch.unapply") {
    PercentCharsMatch.unapply("%") shouldEqual Some(1)
    PercentCharsMatch.unapply("%%") shouldEqual Some(2)
    PercentCharsMatch.unapply("%%%") shouldEqual Some(3)
    PercentCharsMatch.unapply("$") shouldEqual None
    PercentCharsMatch.unapply("%$") shouldEqual None
  }

  test("ModuleIdMatch g %% a % v") {
    val s = parseSource("\"org.something\" %% \"something-name\" % \"0.0.1\"")
    s.stats.head match {
      case ModuleIdMatch(ModuleId(g, p, a, v, Nil)) =>
        g shouldBe "org.something"
        p shouldBe 2
        a shouldBe "something-name"
        v shouldBe "0.0.1"
    }
  }

  test("ModuleIdMatch g %% a % v % Test") {
    val s = parseSource(
      "\"org.something\" %% \"something-name\" % \"0.0.1\" % Test"
    )
    s.stats.head match {
      case ModuleIdMatch(ModuleId(g, p, a, v, terms)) =>
        g shouldBe "org.something"
        p shouldBe 2
        a shouldBe "something-name"
        v shouldBe "0.0.1"
        (terms should have).size(1)
        terms.head.asInstanceOf[Term.Name].value shouldBe "Test"
    }
  }

  test("libraryDependencies +=") {
    val s = parseSource(
      "libraryDependencies += \"org.something\" %% \"something-name\" % \"0.0.1\""
    )
    s.stats.head match {
      case LibraryDependencies(List(moduleId)) =>
        moduleId.groupId shouldBe "org.something"
        moduleId.percentsCount shouldBe 2
        moduleId.artifactId shouldBe "something-name"
        moduleId.version shouldBe "0.0.1"
    }
  }

  test("libraryDependencies ++=") {
    val s = parseSource(
      """libraryDependencies ++= Seq(
        |  "com.github.scopt" %% "scopt" % "3.7.1",
        |  "ch.qos.logback" % "logback-classic" % "1.2.3"
        |)""".stripMargin
    )

    val List(m1, m2) = s.stats.flatMap {
      case LibraryDependencies(list) => list
    }

    m1.groupId shouldBe "com.github.scopt"
    m1.percentsCount shouldBe 2
    m2.groupId shouldBe "ch.qos.logback"
    m2.percentsCount shouldBe 1
  }

}
