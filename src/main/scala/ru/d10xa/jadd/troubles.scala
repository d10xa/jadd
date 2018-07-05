package ru.d10xa.jadd

object troubles {

  type ErrorOrArtifact = Either[ArtifactTrouble, Artifact]

  sealed abstract class ArtifactTrouble

  case class RepositoryUndefined(artifact: Artifact) extends ArtifactTrouble

  case class MetadataLoadTrouble(artifact: Artifact, cause: String) extends ArtifactTrouble

  case class ArtifactNotFoundByAlias(alias: String) extends ArtifactTrouble

  case object WrongArtifactRaw extends ArtifactTrouble

  def handleTroubles(troubles: Seq[ArtifactTrouble], action: String => Unit): Unit = {
    troubles.map {
      case ArtifactNotFoundByAlias(alias) => s"artifact alias not found ($alias)"
      case WrongArtifactRaw => "artifact syntax invalid"
      case MetadataLoadTrouble(url, cause) => s"failed to load metadata from $url ($cause)"
      case RepositoryUndefined(artifact) => s"repository not defined for ${artifact.asPaths}"
    }.foreach(action)
  }

}
