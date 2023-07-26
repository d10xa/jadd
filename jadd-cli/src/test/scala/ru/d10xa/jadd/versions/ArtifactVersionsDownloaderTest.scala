package ru.d10xa.jadd.versions

import cats.data.EitherNel
import cats.effect.IO
import cats.implicits._
import coursier.core.Repository
import coursier.core.Version
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.troubles
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.testkit.TestBase

class ArtifactVersionsDownloaderTest extends TestBase {

  test("version received from repository initialized") {
    val a = ArtifactVersionsDownloader
      .loadArtifactVersions[IO](
        Artifact(groupId = GroupId("a"), artifactId = "b"),
        Seq.empty[String],
        (artifact: Artifact, repositories: Seq[Repository]) =>
          IO(
            artifact
              .copy(
                availableVersions = Seq(Version("42")),
                maybeVersion = Some(Version("42"))
              )
              .asRight
          )
      )
      .unsafeRunSync()
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
          maybeVersion = Some(Version("1.0"))
        ),
        Seq("repo1"),
        new VersionTools[IO] {
          override def loadVersionAndInitLatest(
            artifact: Artifact,
            repositories: Seq[Repository]
          ): IO[EitherNel[troubles.ArtifactTrouble, Artifact]] =
            IO(
              artifact
                .copy(
                  availableVersions = Seq(Version("42")),
                  maybeVersion = Some(Version("42"))
                )
                .asRight
            )
        }
      )
      .unsafeRunSync()
      .right
      .get

    a.maybeVersion.get.repr shouldEqual "1.0"
  }

}
