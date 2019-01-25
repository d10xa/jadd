package ru.d10xa.jadd.versions

import cats.data.EitherNel
import cats.data.NonEmptyList
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.repository.RepositoryApi
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.RepositoryUndefined

trait VersionTools {

  // TODO api refactoring

  def loadVersionAndInitLatest(
    artifact: Artifact): EitherNel[ArtifactTrouble, Artifact]
}

object VersionTools extends VersionTools {

  def loadVersions(artifact: Artifact): EitherNel[ArtifactTrouble, Artifact] = {

    val errOrApi: Either[RepositoryUndefined, RepositoryApi[MavenMetadata]] =
      artifact.repository match {
        case Some(r) => Right(RepositoryApi.fromString(r))
        case None => Left(RepositoryUndefined(artifact))
      }
    val errOrMeta: Either[NonEmptyList[ArtifactTrouble], MavenMetadata] =
      errOrApi.left
        .map(NonEmptyList.one)
        .flatMap(_.receiveRepositoryMeta(artifact))

    val errOrNewArt = errOrMeta.map(artifact.merge)
    errOrNewArt
  }

  override def loadVersionAndInitLatest(
    artifact: Artifact
  ): EitherNel[ArtifactTrouble, Artifact] =
    loadVersions(artifact)
      .map(_.initLatestVersion())

}
