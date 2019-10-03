package ru.d10xa.jadd.versions

import cats.implicits._
import coursier.core.Version
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.GroupId
import ru.d10xa.jadd.testkit.TestBase

class ArtifactVersionsDownloaderTest extends TestBase {

  test("version received from repository initialized") {
    val a = ArtifactVersionsDownloader
      .loadArtifactVersions(
        Artifact(groupId = GroupId("a"), artifactId = "b"),
        Seq("repo1"),
        (artifact: Artifact) =>
          artifact
            .copy(
              availableVersions = Seq(Version("42")),
              maybeVersion = Some(Version("42")))
            .asRight
      )
      .right
      .get

    a.maybeVersion.get.repr shouldEqual "42"
  }

  test("prefer predefined version from artifact") {
    val a = ArtifactVersionsDownloader
      .loadArtifactVersions(
        Artifact(
          groupId = GroupId("a"),
          artifactId = "b",
          maybeVersion = Some(Version("1.0"))),
        Seq("repo1"),
        (artifact: Artifact) =>
          artifact
            .copy(
              availableVersions = Seq(Version("42")),
              maybeVersion = Some(Version("42")))
            .asRight
      )
      .right
      .get

    a.maybeVersion.get.repr shouldEqual "1.0"
  }

}
