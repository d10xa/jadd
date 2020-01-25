package ru.d10xa.jadd

import coursier.core.Version
import ru.d10xa.jadd.core.ArtifactProvider
import ru.d10xa.jadd.core.ArtifactProvider.GradleBuildDescription
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.GroupId
import ru.d10xa.jadd.core.ScalaVersion
import ru.d10xa.jadd.testkit.TestBase

class GradleArtifactProviderTest extends TestBase {

  test("GradleBuildDescription as artifacts") {
    val d = GradleBuildDescription(
      """
        |plugins {
        |    id 'scala'
        |}
        |ext {
        |    scalaTestVersion = '3.0.4'
        |}
        |repositories {
        |    jcenter()
        |}
        |def scalaMajorVersion = "2.12"
        |def scalaVersion = "${scalaMajorVersion}.6"
        |dependencies {
        |    compile "org.scala-lang:scala-library:${scalaVersion}"
        |    testCompile "org.scalatest:scalatest_${scalaMajorVersion}:$scalaTestVersion"
        |}
      """.stripMargin
    )
    val artifacts = ArtifactProvider.gradleArtifactProvider.provide(d)

    artifacts(0) shouldEqual Artifact(
      groupId = GroupId("org.scala-lang"),
      artifactId = "scala-library",
      maybeVersion = Some(Version("2.12.6"))
    )
    artifacts(1) shouldEqual Artifact(
      groupId = GroupId("org.scalatest"),
      artifactId = "scalatest%%",
      maybeVersion = Some(Version("3.0.4")),
      maybeScalaVersion = Some(ScalaVersion.fromString("2.12"))
    )
  }
}
