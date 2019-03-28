package ru.d10xa.jadd.versions

import cats.implicits._
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.testkit.TestBase

class ArtifactVersionsDownloaderTest extends TestBase {

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
