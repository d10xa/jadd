package ru.d10xa.jadd.versions

import cats.implicits._
import org.scalatest.{FunSuite, Matchers}
import ru.d10xa.jadd.Artifact

class ArtifactVersionsDownloaderTest extends FunSuite with Matchers {

  test("version received from repository initialized") {
    val a = ArtifactVersionsDownloader
      .loadArtifactVersions(
        Artifact(groupId = "a", artifactId = "b"),
        Seq("repo1"),
        (artifact: Artifact) =>
          artifact
            .copy(availableVersions = Seq("42"), maybeVersion = Some("42"))
            .asRight
      )
      .right
      .get

    a.maybeVersion.get shouldEqual "42"
  }

  test("prefer predefined version from artifact") {
    val a = ArtifactVersionsDownloader
      .loadArtifactVersions(
        Artifact(groupId = "a", artifactId = "b", maybeVersion = Some("1.0")),
        Seq("repo1"),
        (artifact: Artifact) =>
          artifact
            .copy(availableVersions = Seq("42"), maybeVersion = Some("42"))
            .asRight
      )
      .right
      .get

    a.maybeVersion.get shouldEqual "1.0"
  }

}
