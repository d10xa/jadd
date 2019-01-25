package ru.d10xa.jadd.versions

import cats.data.EitherNel
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles.ArtifactTrouble

object ArtifactVersionsDownloader {

  def loadArtifactVersions(
    artifact: Artifact,
    configRepositories: Seq[String],
    versionTools: VersionTools): EitherNel[ArtifactTrouble, Artifact] =
    if (artifact.maybeVersion.isDefined) {
      Right(artifact)
    } else {
      val artifactWithRepoList: Stream[Artifact] =
        if (artifact.repository.isDefined) Stream(artifact)
        else
          configRepositories.toStream.map(repo =>
            artifact.copy(repository = Some(repo)))
      val res = artifactWithRepoList
        .map(versionTools.loadVersionAndInitLatest)
      res
        .find(_.isRight)
        .getOrElse(res.head)
    }
}
