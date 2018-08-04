package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.ArtifactProvider.GradleBuildDescription

class GradleArtifactProviderTest extends FunSuite with Matchers {

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
      groupId = "org.scala-lang",
      artifactId = "scala-library",
      maybeVersion = Some("2.12.6")
    )
    artifacts(1) shouldEqual Artifact(
      groupId = "org.scalatest",
      artifactId = "scalatest_2.12",
      maybeVersion = Some("3.0.4")
    )
  }
}
