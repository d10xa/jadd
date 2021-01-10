package ru.d10xa.jadd.shortcuts

import cats.effect.Sync
import cats.syntax.all._
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.ArtifactInfo
import ru.d10xa.jadd.core.Scope
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.troubles
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import ru.d10xa.jadd.core.types.GroupId

import java.io.FileNotFoundException
import scala.util.Try

trait ArtifactInfoFinder[F[_]] {
  def artifactFromString(
    artifactShortcuts: ArtifactShortcuts,
    artifactRaw: String
  ): F[Either[ArtifactTrouble, Artifact]]
}

object ArtifactInfoFinder {
  val DEFAULT_ARTIFACT_INFO_BASE_PATH: String = "classpath:artifacts/"

  def make[F[_]: Sync](
    repositoryShortcuts: RepositoryShortcuts,
    artifactInfoBasePath: String =
      ArtifactInfoFinder.DEFAULT_ARTIFACT_INFO_BASE_PATH
  ): F[ArtifactInfoFinder[F]] =
    Sync[F].delay(
      new ArtifactInfoFinderImpl[F](
        repositoryShortcuts,
        artifactInfoBasePath
      )
    )

}

class ArtifactInfoFinderImpl[F[_]: Sync](
  repositoryShortcuts: RepositoryShortcuts,
  artifactInfoBasePath: String
) extends ArtifactInfoFinder[F] {

  import troubles._

  def findArtifactInfo(
    fullArtifact: String
  ): F[Option[ArtifactInfo]] = {

    // if left field is empty then try to add it from right
    def combineEmptyFields(a: ArtifactInfo, b: ArtifactInfo): ArtifactInfo =
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

    def readFile(fileName: String): F[Option[ArtifactInfo]] = {
      import ujson.Value

      implicit class JsOptStr(v: Value) {
        def optStr(selector: String): Option[String] =
          Try(v(selector).str).toOption
      }

      Utils
        .mkStringFromResourceF(artifactInfoBasePath + fileName)
        .map { jsonStr =>
          Try {
            val json = ujson.read(jsonStr)
            ArtifactInfo(
              scope = json.optStr("scope"),
              repository = json.optStr("repository")
            )
          }.toOption
        }
        .recover {
          // https://github.com/scala/scala/pull/8443 Throw
          //   FileNotFoundException in Source.fromResource
          case _: NullPointerException => None
          case _: FileNotFoundException => None // since 2.13.3
        }
    }

    // find file by $groupId:$artifactId.json and then $groupId.json
    val primary: F[Option[ArtifactInfo]] = readFile(
      fullArtifact
        .replaceFirst(":", "__")
        .replaceFirst("%%", "") + ".json"
    )

    val secondary: F[Option[ArtifactInfo]] = readFile(
      fullArtifact.takeWhile(_ != ':') + ".json"
    )

    primary.product(secondary).map {
      case (Some(a), Some(b)) =>
        combineEmptyFields(a, b).some
      case (a, b) =>
        a.orElse(b)
    }
  }

  override def artifactFromString(
    artifactShortcuts: ArtifactShortcuts,
    artifactRaw: String
  ): F[Either[ArtifactTrouble, Artifact]] = {

    val valid: Either[troubles.ArtifactTrouble, Unit] =
      Either
        .cond(!artifactRaw.contains("("), (), WrongArtifactRaw)

    valid
      .flatMap(_ => full(artifactShortcuts)(artifactRaw))
      .traverse((ar: Artifact) => addInfoToArtifact(ar))
  }

  private def shortcutToArtifact(
    artifactShortcuts: ArtifactShortcuts
  )(artifactRaw: String): Option[Artifact] =
    artifactShortcuts
      .unshort(artifactRaw)
      .map(_.split(':'))
      .collect { case Array(a, b) =>
        Artifact(
          groupId = GroupId(a),
          artifactId = b,
          shortcut = Some(artifactRaw)
        )
      }

  private def full(
    artifactShortcuts: ArtifactShortcuts
  )(str: String): Either[ArtifactTrouble, Artifact] =
    if (str.contains(":")) {
      Artifact.fromString(str)
    } else {
      shortcutToArtifact(artifactShortcuts)(str) match {
        case None => Left(ArtifactNotFoundByAlias(str))
        case Some(a) => Right(a)
      }
    }

  private def addInfoToArtifact(a: Artifact): F[Artifact] = {
    val artifactString = s"${a.groupId.show}:${a.artifactId}"
    val maybeInfoF: F[Option[ArtifactInfo]] = findArtifactInfo(artifactString)

    for {
      maybeInfo <- maybeInfoF
      scope = maybeInfo.flatMap(_.scope).map(Scope.scope)
      repositoryPath = maybeInfo
        .flatMap(_.repository)
        .map(repositoryShortcuts.unshortRepository)
    } yield a.copy(scope = scope, repository = repositoryPath)
  }
}
