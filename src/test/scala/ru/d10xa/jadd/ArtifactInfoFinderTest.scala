package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder

class ArtifactInfoFinderTest extends FunSuite with Matchers {

  val artifactInfoFinder: ArtifactInfoFinder = new ArtifactInfoFinder()

  import artifactInfoFinder._

  test("with scope") {
    artifactFromString("junit") shouldEqual Artifact(
      groupId = "junit",
      artifactId = "junit",
      shortcut = Some("junit"),
      scope = Some(Scope.Test)
    )
  }

  test("find existent artifact info") {
    findArtifactInfo("junit:junit") shouldEqual Some(ArtifactInfo(
      groupId = "junit",
      artifactId = "junit",
      scope = Some("test")
    ))
  }

  test("find non-existent artifact info") {
    findArtifactInfo("com.example:example") shouldEqual None
  }

}
