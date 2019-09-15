package ru.d10xa.jadd

import cats.effect.IO
import coursier.core.Version
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts.ArtifactShortcutsClasspath
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.troubles.ArtifactNotFoundByAlias

class ArtifactInfoFinderTest extends TestBase {

  val artifactInfoFinder: ArtifactInfoFinder = new ArtifactInfoFinder(
    artifactShortcuts = ArtifactShortcutsClasspath,
    repositoryShortcuts = RepositoryShortcutsImpl
  )

  import artifactInfoFinder._

  test("with scope") {
    artifactFromString[IO]("junit")
      .unsafeRunSync()
      .right
      .get shouldEqual Artifact(
      groupId = "junit",
      artifactId = "junit",
      shortcut = Some("junit"),
      scope = Some(Scope.Test)
    )
  }

  test("find existent artifact info") {
    findArtifactInfo[IO]("junit:junit").unsafeRunSync() shouldEqual Some(
      ArtifactInfo(
        scope = Some("test"),
        repository = None
      ))
  }

  test("find artifactInfo with bintray repository") {
    findArtifactInfo[IO]("de.heikoseeberger:akka-http-circe%%")
      .unsafeRunSync() shouldEqual Some(
      ArtifactInfo(
        scope = None,
        repository = Some("bintray/hseeberger/maven")
      ))
  }

  test("find artifact with bintray repository") {
    artifactFromString[IO]("de.heikoseeberger:akka-http-circe%%")
      .unsafeRunSync()
      .right
      .get shouldEqual Artifact(
      groupId = "de.heikoseeberger",
      artifactId = "akka-http-circe%%",
      scope = None,
      repository = Some("https://dl.bintray.com/hseeberger/maven")
    )
  }

  test("find non-existent artifact info") {
    findArtifactInfo[IO]("com.example:example")
      .unsafeRunSync() shouldEqual None
  }

  test("unknown shortcut") {
    artifactFromString[IO]("safojasfoi")
      .unsafeRunSync()
      .left
      .get shouldEqual ArtifactNotFoundByAlias("safojasfoi")
  }

  test("aware scala version") {
    val a =
      artifactFromString[IO]("a:b_2.12:1.1.0").unsafeRunSync().right.get
    a shouldEqual Artifact(
      groupId = "a",
      artifactId = "b%%",
      maybeScalaVersion = Some("2.12"),
      maybeVersion = Some(Version("1.1.0"))
    )
  }

}
