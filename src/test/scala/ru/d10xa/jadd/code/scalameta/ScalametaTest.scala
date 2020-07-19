package ru.d10xa.jadd.code.scalameta

import ru.d10xa.jadd.testkit.TestBase

import scala.meta.Term

class ScalametaTest extends TestBase {

  val sbtModuleIdFinder: SbtModuleIdFinder = SbtModuleIdFinder
  val sbtStringValFinder: SbtStringValFinder = SbtStringValFinder

  def findModules(str: String): List[ModuleId] =
    sbtModuleIdFinder.find(parseSource(str))

  def findStringVals(str: String): List[StringVal] =
    sbtStringValFinder.find(parseSource(str))

  test("PercentCharsMatch.unapply") {
    PercentCharsMatch.unapply("%") shouldEqual Some(1)
    PercentCharsMatch.unapply("%%") shouldEqual Some(2)
    PercentCharsMatch.unapply("%%%") shouldEqual Some(3)
    PercentCharsMatch.unapply("$") shouldEqual None
    PercentCharsMatch.unapply("%$") shouldEqual None
  }

  test("ModuleIdMatch g %% a % v") {
    val List(moduleId) =
      findModules("\"org.something\" %% \"something-name\" % \"0.0.1\"")
    moduleId.groupId shouldBe "org.something"
    moduleId.percentsCount shouldBe 2
    moduleId.artifactId shouldBe "something-name"
    moduleId.version shouldBe VersionString("0.0.1")
  }

  test("ModuleIdMatch g %% a % v % Test") {
    val List(moduleId) = findModules(
      "\"org.something\" %% \"something-name\" % \"0.0.1\" % Test"
    )
    moduleId.groupId shouldBe "org.something"
    moduleId.percentsCount shouldBe 2
    moduleId.artifactId shouldBe "something-name"
    moduleId.version shouldBe VersionString("0.0.1")
    (moduleId.terms should have).size(1)
    moduleId.terms match {
      case List(name: Term.Name) => name.value shouldBe "Test"
    }
  }

  test("ModuleIdMatch VersionVal") {
    val List(moduleId) = findModules(
      "\"org.something\" %% \"something-name\" % somethingVersion % Test"
    )
    moduleId.version shouldBe VersionVal("somethingVersion")
    moduleId.terms match {
      case List(name: Term.Name) => name.value shouldBe "Test"
    }
  }

  test("libraryDependencies +=") {
    val List(moduleId): List[ModuleId] = findModules(
      "libraryDependencies += \"org.something\" %% \"something-name\" % \"0.0.1\""
    )
    moduleId.groupId shouldBe "org.something"
    moduleId.percentsCount shouldBe 2
    moduleId.artifactId shouldBe "something-name"
    moduleId.version shouldBe VersionString("0.0.1")
  }

  test("libraryDependencies ++=") {
    val moduleIds: List[ModuleId] =
      findModules("""libraryDependencies ++= Seq(
        |  "com.github.scopt" %% "scopt" % "3.7.1",
        |  "ch.qos.logback" % "logback-classic" % "1.2.3"
        |)""".stripMargin)

    val List(m1, m2) = moduleIds

    m1.groupId shouldBe "com.github.scopt"
    m1.percentsCount shouldBe 2
    m2.groupId shouldBe "ch.qos.logback"
    m2.percentsCount shouldBe 1
  }

  test("find val string in sbt file") {
    val vals = findStringVals("""
        |val catsVersion = "1.0.0"
        |def x = "???"
        |""".stripMargin)

    val List(v) = vals
    v.name shouldBe "catsVersion"
    v.value shouldBe "1.0.0"
  }

}
