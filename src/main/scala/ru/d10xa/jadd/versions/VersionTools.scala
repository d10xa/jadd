package ru.d10xa.jadd.versions

import cats.data.EitherNel
import cats.data.NonEmptyList
import cats.implicits._
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.repository.RepositoryApi
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.RepositoryUndefined

trait VersionTools {

  // TODO api refactoring

  def loadVersionAndInitLatest(
    artifact: Artifact): EitherNel[ArtifactTrouble, Artifact]
}

object VersionTools extends VersionTools {

  def repositoryApiFromArtifact(
    artifact: Artifact
  ): Either[ArtifactTrouble, RepositoryApi] =
    artifact.repository match {
      case Some(r) => Right(RepositoryApi.fromString(r))
      case None => Left(RepositoryUndefined(artifact))
    }

  val repositoryApiFromArtifactNelErr
    : Artifact => EitherNel[ArtifactTrouble, RepositoryApi] =
    (repositoryApiFromArtifact _)
      .rmap(_.leftMap(NonEmptyList.one))

  def loadVersions(artifact: Artifact): EitherNel[ArtifactTrouble, Artifact] =
    repositoryApiFromArtifactNelErr(artifact)
      .flatMap(_.receiveRepositoryMeta(artifact))
      .map(artifact.merge)

  override def loadVersionAndInitLatest(
    artifact: Artifact
  ): EitherNel[ArtifactTrouble, Artifact] =
    loadVersions(artifact)
      .map(_.initLatestVersion())

}
