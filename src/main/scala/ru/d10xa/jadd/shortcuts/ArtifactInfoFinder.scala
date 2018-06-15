package ru.d10xa.jadd.shortcuts

import cats.syntax.either._
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.ArtifactInfo
import ru.d10xa.jadd.Scope
import ru.d10xa.jadd.Utils

import scala.io.BufferedSource
import scala.util.Try

class ArtifactInfoFinder(
  artifactShortcuts: ArtifactShortcuts = new ArtifactShortcuts(),
  repositoryShortcuts: RepositoryShortcuts = RepositoryShortcutsImpl,
  artifactInfoBasePath: String = ArtifactInfoFinder.DEFAULT_ARTIFACT_INFO_BASE_PATH
) {

  import ru.d10xa.jadd.troubles._

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

  def artifactFromString(artifactRaw: String): Either[ArtifactTrouble, Artifact] = {
    require(!artifactRaw.contains("("), "artifact contain illegal symbol (")

    def shortcutToArtifact: Option[Artifact] =
      artifactShortcuts.unshort(artifactRaw)
        .map(_.split(':'))
        .collect {
          case Array(a, b) =>
            Artifact(groupId = a, artifactId = b, shortcut = Some(artifactRaw))
        }

    val artifact: Either[ArtifactTrouble, Artifact] =
      if (artifactRaw.contains(":")) Artifact.fromString(artifactRaw)
      else Either.fromOption(shortcutToArtifact, ArtifactNotFoundByAlias(artifactRaw))

    def addInfoToArtifact(a: Artifact): Artifact = {
      val artifactString = s"${a.groupId}:${a.artifactId}"
      val maybeInfo: Option[ArtifactInfo] = findArtifactInfo(artifactString)

      val scope: Option[Scope] =
        maybeInfo.flatMap(_.scope).map(Scope.scope)

      val repositoryPath: Option[String] =
        maybeInfo
          .flatMap(_.repository)
          .map(repositoryShortcuts.unshortRepository)
      a.copy(scope = scope, repository = repositoryPath)
    }

    artifact.map(addInfoToArtifact)
  }

}

object ArtifactInfoFinder {
  val DEFAULT_ARTIFACT_INFO_BASE_PATH = "classpath:artifacts/"
}
