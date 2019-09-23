package ru.d10xa.jadd.versions

import cats.data.EitherNel
import cats.data.IorNel
import cats.implicits._
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles.ArtifactTrouble

object ArtifactVersionsDownloader {

  def loadArtifactVersions(
    artifact: Artifact,
    configRepositories: Seq[String],
    versionTools: VersionTools): IorNel[ArtifactTrouble, Artifact] =
    if (artifact.maybeVersion.isDefined) {
      artifact.rightIor
    } else {
      loadArtifactVersionsForce(artifact, configRepositories, versionTools)
    }

  def loadArtifactVersionsForce(
    artifact: Artifact,
    configRepositories: Seq[String], // TODO ValueClass for repository representation
    versionTools: VersionTools): IorNel[ArtifactTrouble, Artifact] = {
    val artifactWithRepoList: LazyList[Artifact] =
      if (artifact.repository.isDefined) LazyList(artifact)
      else
        LazyList
          .from(configRepositories)
          .map(repo => artifact.copy(repository = Some(repo)))
    val res: Seq[EitherNel[ArtifactTrouble, Artifact]] =
      artifactWithRepoList
        .map(versionTools.loadVersionAndInitLatest)

    val o: Option[EitherNel[ArtifactTrouble, Artifact]] = res
      .find(_.isRight)

    o match {
      case Some(either) =>
        either.toIor
      case None =>
        res
          .collect { case Left(e) => e }
          .flatMap(_.toList)
          .toList
          .toNel
          .get
          .leftIor[Artifact]
    }
  }
}
