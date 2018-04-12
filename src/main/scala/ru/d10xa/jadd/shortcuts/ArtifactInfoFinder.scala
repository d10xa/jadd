package ru.d10xa.jadd.shortcuts

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.ArtifactInfo
import ru.d10xa.jadd.Scope
import ru.d10xa.jadd.Utils

import scala.io.BufferedSource
import scala.io.Source
import scala.util.Try

class ArtifactInfoFinder(
  source: Source = Source.fromResource("jadd-shortcuts.csv"),
  artifactInfoBasePath: String = "classpath:artifacts/"
) {

  import ArtifactInfoFinder._

  lazy val shortcuts: Map[String, String] = {
    val lines = source
      .getLines()
      .toSeq
    val linesWithoutHeader =
      if (lines.head == "shortcut,artifact") lines.tail else lines
    linesWithoutHeader
      .map(_.split(','))
      .map {
        case Array(short, full) => (short, full)
      }
      .toMap
  }

  lazy val shortcutsReversed: Map[String, String] =
    shortcuts.toSeq.map { case (a, b) => (b, a) }.toMap

  def unshort(rawArtifact: String): Option[String] =
    shortcuts.get(rawArtifact)

  def findArtifactInfo(fullArtifact: String): Option[ArtifactInfo] = {


    // if left field is empty then try to add it from right
    def combineEmptyFields(a: ArtifactInfo, b: ArtifactInfo): ArtifactInfo = {
      a.copy(
        repository = a.repository match {
          case o if o.isDefined => o
          case _ => b.repository
        },
        scope = a.scope match {
          case o if o.isDefined => o
          case _ => b.scope
        }
      )
    }

    def readFile(fileName: String): Option[ArtifactInfo] = {
      import io.circe.generic.auto._
      import io.circe.parser._

      val artifactInfoPath: String = artifactInfoBasePath + fileName

      val source: BufferedSource = Utils.sourceFromSpringUri(artifactInfoPath)
      if (Try(source.hasNext).recover { case _: NullPointerException => false }.get) { // TODO get
        decode[ArtifactInfo](source.mkString).toOption
      } else {
        None
      }
    }

    // find file by $groupId:$artifactId.json and then $groupId.json
    val primary: Option[ArtifactInfo] = readFile(fullArtifact.replaceFirst(":", "__")
      .replaceFirst("%%", "") + ".json")

    val secondary: Option[ArtifactInfo] = readFile(fullArtifact.takeWhile(_ != ':') + ".json")

    (primary, secondary) match {
      case (Some(a), Some(b)) => Some(combineEmptyFields(a, b))
      case (a, b) => a.orElse(b)
    }
  }

  def artifactFromString(artifactRaw: String): Artifact = {
    require(!artifactRaw.contains("("), "artifact contain illegal symbol (")

    def shortcutToArtifact: Option[Artifact] =
      unshort(artifactRaw)
        .map(s => s.split(":"))
        .collect {
          case Array(a, b) =>
            Artifact(groupId = a, artifactId = b, shortcut = Some(artifactRaw))
        }

    def fullToArtifact: Artifact =
      artifactRaw.split(":") match {
        case Array(a, b) =>
          Artifact(
            groupId = a,
            artifactId = b
          )
      }

    val artifact =
      shortcutToArtifact getOrElse fullToArtifact

    val artifactString = s"${artifact.groupId}:${artifact.artifactId}"
    val maybeInfo: Option[ArtifactInfo] = findArtifactInfo(artifactString)

    val scope: Option[Scope] =
      maybeInfo.flatMap(_.scope).map(Scope.scope)

    val repositoryPath: Option[String] =
      maybeInfo
        .flatMap(_.repository)
        .map(unshortRepository)

    artifact.copy(scope = scope, repositoryPath = repositoryPath)
  }

}

object ArtifactInfoFinder {
  def unshortRepository(repo: String): String = {
    if (repo.startsWith("bintray/")) s"https://dl.bintray.com/${repo.drop(8)}"
    else if (repo == "mavenCentral") "http://central.maven.org/maven2"
    else if (repo == "jcenter") "https://jcenter.bintray.com"
    else if (repo == "google") "https://dl.google.com/dl/android/maven2"
    else repo
  }
}
