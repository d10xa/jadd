package ru.d10xa.jadd

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
    artifactFromString("junit").right.get shouldEqual Artifact(
      groupId = "junit",
      artifactId = "junit",
      shortcut = Some("junit"),
      scope = Some(Scope.Test)
    )
  }

  test("find existent artifact info") {
    findArtifactInfo("junit:junit") shouldEqual Some(
      ArtifactInfo(
        scope = Some("test"),
        repository = None
      ))
  }

  test("find artifactInfo with bintray repository") {
    findArtifactInfo("de.heikoseeberger:akka-http-circe%%") shouldEqual Some(
      ArtifactInfo(
        scope = None,
        repository = Some("bintray/hseeberger/maven")
      ))
  }

  test("find artifact with bintray repository") {
    artifactFromString("de.heikoseeberger:akka-http-circe%%").right.get shouldEqual Artifact(
      groupId = "de.heikoseeberger",
      artifactId = "akka-http-circe%%",
      scope = None,
      repository = Some("https://dl.bintray.com/hseeberger/maven")
    )
  }

  test("find non-existent artifact info") {
    findArtifactInfo("com.example:example") shouldEqual None
  }

  test("unknown shortcut") {
    artifactFromString("safojasfoi").left.get shouldEqual ArtifactNotFoundByAlias(
      "safojasfoi")
  }

  test("aware scala version") {
    val a = artifactFromString("a:b_2.12:1.1.0").right.get
    a shouldEqual Artifact(
      groupId = "a",
      artifactId = "b%%",
      maybeScalaVersion = Some("2.12"),
      maybeVersion = Some(Version("1.1.0"))
    )
  }

}
