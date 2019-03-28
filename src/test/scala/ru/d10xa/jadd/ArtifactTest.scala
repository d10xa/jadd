package ru.d10xa.jadd

import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.troubles.WrongArtifactRaw

class ArtifactTest extends TestBase {

  test("apply single string without version") {
    val a = art("org.seleniumhq.selenium:selenium-api")
    a.groupId shouldEqual "org.seleniumhq.selenium"
    a.artifactId shouldEqual "selenium-api"
    a.maybeVersion shouldEqual None
  }

  test("apply single string with version") {
    val a = art("org.seleniumhq.selenium:selenium-api:3.0.0")

    a.groupId shouldEqual "org.seleniumhq.selenium"
    a.artifactId shouldEqual "selenium-api"
    a.maybeVersion shouldEqual Some("3.0.0")
  }

  test("asPath") {
    val a1 = art("org.seleniumhq.selenium:selenium-api")
    val a2 = art("org.seleniumhq.selenium:selenium-api:3.0.0")

    a1.asPath shouldEqual "org/seleniumhq/selenium/selenium-api"
    a2.asPath shouldEqual "org/seleniumhq/selenium/selenium-api"
  }

  test("needScalaVersionResolving") {
    art("org.scala-lang.modules:scala-async%%").needScalaVersionResolving shouldEqual true
    art("org.jline:jline").needScalaVersionResolving shouldEqual false
  }

  test("withMetadataUrl") {
    val a = art("junit:junit")
    a.mavenMetadata shouldEqual None

    val metadataUrl =
      "https://jcenter.bintray.com/junit/junit/maven-metadata.xml"
    a.withMetadataUrl(metadataUrl).mavenMetadata shouldEqual Some(
      MavenMetadata(url = Some(metadataUrl)))
  }

  test("fromString wrong") {
    Artifact.fromString("a:b:1.0").isRight shouldEqual true
    Artifact.fromString("a:b:c:d").left.get shouldEqual WrongArtifactRaw
    Artifact
      .fromString("only-groupid-or-shortcut")
      .left
      .get shouldEqual WrongArtifactRaw
  }

  test("fromTuple") {
    Artifact.fromTuple3(("a", "b", "1.0")) shouldEqual Artifact(
      "a",
      "b",
      Some("1.0"))
    Artifact.fromTuple2(("a", "b")) shouldEqual Artifact("a", "b", None)
  }

}
