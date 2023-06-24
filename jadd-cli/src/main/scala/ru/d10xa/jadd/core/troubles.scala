package ru.d10xa.jadd.core

import cats.Applicative
import cats.Show
import cats.syntax.all._
import cats.data.EitherNel
import ru.d10xa.jadd.log.Logger

object troubles {

  type ErrorOrArtifact = Either[ArtifactTrouble, Artifact]

  sealed abstract class ArtifactTrouble

  object ArtifactTrouble {
    implicit val showArtifactTrouble: Show[ArtifactTrouble] = {
      case ArtifactNotFoundByAlias(alias) =>
        s"artifact alias not found ($alias)"
      case WrongArtifactRaw => "artifact syntax invalid"
      case MetadataLoadTrouble(artifact, cause) =>
        s"failed to load metadata for ${artifact.show} ($cause)"
      case RepositoryUndefined(artifact) =>
        val gId = artifact.groupId.show
        val aId = artifact.artifactId
        s"repository not defined for $gId:$aId"
    }
  }

  final case class RepositoryUndefined(artifact: Artifact)
      extends ArtifactTrouble

  final case class MetadataLoadTrouble(artifact: Artifact, cause: String)
      extends ArtifactTrouble

  final case class ArtifactNotFoundByAlias(alias: String)
      extends ArtifactTrouble

  case object WrongArtifactRaw extends ArtifactTrouble

  def extractTroublesOnly[F[_]: Applicative](
    artifacts: Seq[EitherNel[ArtifactTrouble, Artifact]]
  ): Seq[ArtifactTrouble] =
    artifacts.flatMap {
      case Right(_) => Seq.empty
      case Left(t) => t.toList
    }

  def logTroubles[F[_]: Applicative](
    troubles: Seq[ArtifactTrouble]
  )(implicit logger: Logger[F]): F[Unit] =
    troubles
      .map(_.show)
      .traverse_(s => logger.info[String](s))

}
