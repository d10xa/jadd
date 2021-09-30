package ru.d10xa.jadd.code.scalameta

import cats.effect.SyncIO
import ru.d10xa.jadd.testkit.TestBase

import scala.meta.Source
import scala.meta.Term
import scala.meta.dialects
import org.scalatest.EitherValues._
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Module
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyPercentChars
import ru.d10xa.jadd.code.scalameta.ScalametaUtils.replacePositions

import java.nio.file.Paths
import scala.meta.inputs.Input
import scala.meta.inputs.Position

class ScalametaTest extends TestBase {

  private val sbtModuleParser =
    SbtModuleParser.make[SyncIO]().unsafeRunSync()

  def findModules(str: String): Vector[Module] =
    sbtModuleParser
      .parse(
        Vector(
          Paths.get(".") -> dialects.Sbt1(str).parse[Source].toEither.value
        )
      )
      .unsafeRunSync()

  implicit class VariableValueOps(s: VariableValue) {
    def value: String = s match {
      case VariableLit(value, _) => value
      case v: VariableLitP => v.lit.value
      case VariableTerms(values) =>
        throw new IllegalArgumentException(
          s"SString is TermNameCompound $values"
        )
    }
  }

  test("PercentCharsMatch.unapply") {
    UnapplyPercentChars.unapply("%") shouldEqual Some(1)
    UnapplyPercentChars.unapply("%%") shouldEqual Some(2)
    UnapplyPercentChars.unapply("%%%") shouldEqual Some(3)
    UnapplyPercentChars.unapply("$") shouldEqual None
    UnapplyPercentChars.unapply("%$") shouldEqual None
  }

  test("ModuleIdMatch g %% a % v") {
    val Vector(moduleId) =
      findModules("\"org.something\" %% \"something-name\" % \"0.0.1\"")
    moduleId.groupId.value shouldBe "org.something"
    moduleId.percentsCount shouldBe 2
    moduleId.artifactId.value shouldBe "something-name"
    moduleId.version.value shouldBe "0.0.1"
  }

  test("ModuleIdMatch g %% a % v % Test") {
    val Vector(moduleId) = findModules(
      "\"org.something\" %% \"something-name\" % \"0.0.1\" % Test"
    )
    moduleId.groupId.value shouldBe "org.something"
    moduleId.percentsCount shouldBe 2
    moduleId.artifactId.value shouldBe "something-name"
    moduleId.version.value shouldBe "0.0.1"
    (moduleId.terms should have).size(1)
    moduleId.terms match {
      case List(name: Term.Name) => name.value shouldBe "Test"
    }
  }

  test("ModuleIdMatch VersionVal") {
    val Vector(moduleId) = findModules(
      "\"org.something\" %% \"something-name\" % somethingVersion % Test"
    )
    moduleId.version shouldBe VariableTerms(Vector("somethingVersion"))
    moduleId.terms match {
      case List(name: Term.Name) => name.value shouldBe "Test"
    }
  }

  test("libraryDependencies +=") {
    val Vector(moduleId) = findModules(
      "libraryDependencies += \"org.something\" %% \"something-name\" % \"0.0.1\""
    )
    moduleId.groupId.value shouldBe "org.something"
    moduleId.percentsCount shouldBe 2
    moduleId.artifactId.value shouldBe "something-name"
    moduleId.version.value shouldBe "0.0.1"
  }

  test("libraryDependencies ++=") {
    val moduleIds: Vector[Module] =
      findModules("""
        |libraryDependencies ++= Seq(
        |  "com.github.scopt" %% "scopt" % "3.7.1",
        |  "ch.qos.logback" % "logback-classic" % "1.2.3"
        |)
        |libraryDependencies += "org.jsoup" % "jsoup" % "1.14.3"
        |""".stripMargin)

    val Vector(m1, m2, m3) = moduleIds

    m1.groupId.value shouldBe "com.github.scopt"
    m1.percentsCount shouldBe 2
    m2.groupId.value shouldBe "ch.qos.logback"
    m2.percentsCount shouldBe 1
    m3.groupId.value shouldBe "org.jsoup"
    m3.percentsCount shouldBe 1
  }

  test("replacePositions") {
    val newStr =
      replacePositions(
        "12 34 56",
        List(
          (Position.Range(Input.String(""), 0, 2), "1"),
          (Position.Range(Input.String(""), 3, 5), "222"),
          (Position.Range(Input.String(""), 6, 8), "333")
        )
      )
    newStr shouldBe "1 222 333"
  }

}
