package ru.d10xa.jadd.versions

import cats.{MonadError, MonadThrow}
import cats.syntax.all._
import cats.data.IorNel
import cats.data.NonEmptyList
import coursier.core.Repository
import coursier.parse.RepositoryParser
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import ru.d10xa.jadd.extensions.CoursierValidationNelExtension
import ru.d10xa.jadd.extensions.ValidatedNelStringOps

object ArtifactVersionsDownloader {

  def loadArtifactVersions[F[_]: MonadThrow](
    artifact: Artifact,
    configRepositories: Seq[String],
    versionTools: VersionTools[F]
  ): F[IorNel[ArtifactTrouble, Artifact]] =
    if (artifact.maybeVersion.isDefined) {
      artifact.rightIor[NonEmptyList[ArtifactTrouble]].pure[F]
    } else {
      loadArtifactVersionsForce(artifact, configRepositories, versionTools)
    }

  private def repositoriesCustomOrDefault[F[_]: MonadThrow](
    artifact: Artifact,
    defaultRepositories: Seq[String]
  ): F[Seq[Repository]] =
    artifact.repository match {
      case Some(repository) =>
        MonadError[F, Throwable].fromEither(
          RepositoryParser
            .repository(repository)
            .map(r => Seq(r))
            .leftMap(new RuntimeException(_))
            .leftWiden[Throwable]
        )
      case None =>
        MonadError[F, Throwable].fromValidated(
          RepositoryParser
            .repositories(defaultRepositories)
            .toCatsValidatedNel
            .joinNel
            .leftMap(new RuntimeException(_))
            .leftWiden[Throwable]
        )
    }

  def loadArtifactVersionsForce[F[_]: MonadThrow](
    artifact: Artifact,
    configRepositories: Seq[String],
    versionTools: VersionTools[F]
  ): F[IorNel[ArtifactTrouble, Artifact]] =
    for {
      repositories <- repositoriesCustomOrDefault[F](
        artifact,
        configRepositories
      )
      res <- versionTools.loadVersionAndInitLatest(artifact, repositories)
    } yield res.toIor

}
