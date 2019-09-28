package ru.d10xa.jadd

import cats.data.EitherNel
import cats.implicits._

object troubles {

  type ErrorOrArtifact = Either[ArtifactTrouble, Artifact]

  sealed abstract class ArtifactTrouble

  final case class RepositoryUndefined(artifact: Artifact)
      extends ArtifactTrouble

  final case class MetadataLoadTrouble(artifact: Artifact, cause: String)
      extends ArtifactTrouble

  final case class ArtifactNotFoundByAlias(alias: String)
      extends ArtifactTrouble

  case object WrongArtifactRaw extends ArtifactTrouble

  def findAndHandleTroubles(
    artifacts: Seq[EitherNel[ArtifactTrouble, Artifact]],
    action: String => Unit): Unit = {
    val troubles = artifacts.flatMap { e =>
      e match {
        case Right(_) => Seq.empty
        case Left(t) => t.toList
      }
    }
    handleTroubles(troubles, action)
  }

  def handleTroubles(
    troubles: Seq[ArtifactTrouble],
    action: String => Unit): Unit =
    troubles
      .map {
        case ArtifactNotFoundByAlias(alias) =>
          s"artifact alias not found ($alias)"
        case WrongArtifactRaw => "artifact syntax invalid"
        case MetadataLoadTrouble(artifact, cause) =>
          s"failed to load metadata for ${artifact.show} ($cause)"
        case RepositoryUndefined(artifact) =>
          s"repository not defined for ${artifact.groupId}:${artifact.artifactId}"
      }
      .foreach(action)

}
