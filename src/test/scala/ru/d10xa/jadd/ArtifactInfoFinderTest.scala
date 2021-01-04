package ru.d10xa.jadd

import cats.effect.IO
import coursier.core.Version
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts.ArtifactShortcutsClasspath
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.core.troubles.ArtifactNotFoundByAlias
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import cats.implicits._
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.ArtifactInfo
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.core.Scope

class ArtifactInfoFinderTest extends TestBase {

  val artifactInfoFinder: ArtifactInfoFinder = new ArtifactInfoFinder(
    artifactShortcuts = ArtifactShortcutsClasspath,
    repositoryShortcuts = RepositoryShortcutsImpl
  )

  import artifactInfoFinder._

  test("with scope") {
    artifactFromString[IO]("junit")
      .unsafeRunSync() shouldEqual Artifact(
      groupId = GroupId("junit"),
      artifactId = "junit",
      shortcut = Some("junit"),
      scope = Some(Scope.Test)
    ).asRight[ArtifactTrouble]
  }

  test("find existent artifact info") {
    findArtifactInfo[IO]("junit:junit").unsafeRunSync() shouldEqual Some(
      ArtifactInfo(
        scope = Some("test"),
        repository = None
      )
    )
  }

  test("find artifactInfo with bintray repository") {
    findArtifactInfo[IO]("de.heikoseeberger:akka-http-circe%%")
      .unsafeRunSync() shouldEqual Some(
      ArtifactInfo(
        scope = None,
        repository = Some("bintray/hseeberger/maven")
      )
    )
  }

  test("find artifact with bintray repository") {
    artifactFromString[IO]("de.heikoseeberger:akka-http-circe%%")
      .unsafeRunSync() shouldEqual Artifact(
      groupId = GroupId("de.heikoseeberger"),
      artifactId = "akka-http-circe%%",
      scope = None,
      repository = Some("https://dl.bintray.com/hseeberger/maven")
    ).asRight[ArtifactTrouble]
  }

  test("find non-existent artifact info") {
    findArtifactInfo[IO]("com.example:example")
      .unsafeRunSync() shouldEqual None
  }

  test("unknown shortcut") {
    artifactFromString[IO]("safojasfoi")
      .unsafeRunSync() shouldEqual ArtifactNotFoundByAlias("safojasfoi")
      .asLeft[Artifact]
  }

  test("aware scala version") {
    val a =
      artifactFromString[IO]("a:b_2.12:1.1.0").unsafeRunSync().toOption.get
    a shouldEqual Artifact(
      groupId = GroupId("a"),
      artifactId = "b%%",
      maybeScalaVersion = Some(ScalaVersion.fromString("2.12")),
      maybeVersion = Some(Version("1.1.0"))
    )
  }

}
