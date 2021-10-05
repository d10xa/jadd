package ru.d10xa.jadd

import coursier.core.Version
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.core.troubles.WrongArtifactRaw
import cats.implicits._
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion

class ArtifactTest extends TestBase {

  test("apply single string without version") {
    val a = art("org.seleniumhq.selenium:selenium-api")
    a.groupId shouldEqual GroupId("org.seleniumhq.selenium")
    a.artifactId shouldEqual "selenium-api"
    a.maybeVersion shouldEqual None
  }

  test("apply single string with version") {
    val a = art("org.seleniumhq.selenium:selenium-api:3.0.0")

    a.groupId shouldEqual GroupId("org.seleniumhq.selenium")
    a.artifactId shouldEqual "selenium-api"
    a.maybeVersion.map(_.repr) shouldEqual Some("3.0.0")
  }

  test("asPath") {
    val a1 = art("org.seleniumhq.selenium:selenium-api")
    val a2 = art("org.seleniumhq.selenium:selenium-api:3.0.0")

    a1.asPath shouldEqual "org/seleniumhq/selenium/selenium-api"
    a2.asPath shouldEqual "org/seleniumhq/selenium/selenium-api"
  }

  test("needScalaVersionResolving") {
    art(
      "org.scala-lang.modules:scala-async%%"
    ).needScalaVersionResolving shouldEqual true
    art("org.jline:jline").needScalaVersionResolving shouldEqual false
  }

  test("fromString wrong") {
    Artifact.fromString("a:b:1.0").isRight shouldEqual true
    Artifact.fromString("a:b:c:d") shouldEqual WrongArtifactRaw.asLeft[Artifact]
    Artifact
      .fromString("only-groupid-or-shortcut") shouldEqual WrongArtifactRaw
      .asLeft[Artifact]
  }

  test("fromTuple") {
    Artifact.fromTuple3(("a", "b", "1.0")) shouldEqual Artifact(
      GroupId("a"),
      "b",
      Some(Version("1.0"))
    )
    Artifact
      .fromTuple2(("a", "b")) shouldEqual Artifact(GroupId("a"), "b", None)
  }

  test("scalaVersionAsPlaceholders") {
    val (artifactId, maybeScalaVersion) =
      Artifact.scalaVersionAsPlaceholders("b_2.12")
    artifactId shouldEqual "b%%"
    maybeScalaVersion shouldEqual ScalaVersion.fromString("2.12").some
  }

}
