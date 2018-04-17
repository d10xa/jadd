package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.ArtifactNotFoundByAlias

class ArtifactInfoFinderTest extends FunSuite with Matchers {

  val artifactInfoFinder: ArtifactInfoFinder = new ArtifactInfoFinder()

  import artifactInfoFinder._

  test("with scope") {
    artifactFromString("junit").right.get shouldEqual Artifact(
      groupId = "junit",
      artifactId = "junit",
      shortcut = Some("junit"),
      scope = Some(Scope.Test)
    )
  }

  test("find existent artifact info") {
    findArtifactInfo("junit:junit") shouldEqual Some(ArtifactInfo(
      scope = Some("test"),
      repository = None
    ))
  }

  test("find artifactInfo with bintray repository") {
    findArtifactInfo("de.heikoseeberger:akka-http-circe%%") shouldEqual Some(ArtifactInfo(
      scope = None,
      repository = Some("bintray/hseeberger/maven")
    ))
  }

  test("find artifact with bintray repository") {
    artifactFromString("de.heikoseeberger:akka-http-circe%%").right.get shouldEqual Artifact(
      groupId = "de.heikoseeberger",
      artifactId = "akka-http-circe%%",
      scope = None,
      repositoryPath = Some("https://dl.bintray.com/hseeberger/maven")
    )
  }

  test("find non-existent artifact info") {
    findArtifactInfo("com.example:example") shouldEqual None
  }

  test("unknown shortcut"){
    artifactFromString("safojasfoi").left.get shouldEqual ArtifactNotFoundByAlias
  }

}
