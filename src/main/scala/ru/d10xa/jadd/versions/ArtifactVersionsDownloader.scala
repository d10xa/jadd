package ru.d10xa.jadd.versions

import cats.data.EitherNel
import cats.data.IorNel
import cats.data.NonEmptyList
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
      val artifactWithRepoList: Stream[Artifact] =
        if (artifact.repository.isDefined) Stream(artifact)
        else
          configRepositories.toStream.map(repo =>
            artifact.copy(repository = Some(repo)))
      val res: Seq[EitherNel[ArtifactTrouble, Artifact]] =
        artifactWithRepoList
          .map(versionTools.loadVersionAndInitLatest)

      val o: Option[EitherNel[ArtifactTrouble, Artifact]] = res
        .find(_.isRight)

      // right.get is safe because of .find(_.isRight)
      o match {
        case Some(either) =>
          either.right.get
            .rightIor[NonEmptyList[ArtifactTrouble]]
        case None =>
          res
            .collect { case e if e.isLeft => e.left.get }
            .flatMap(_.toList)
            .toList
            .toNel
            .get
            .leftIor[Artifact]
      }
    }
}
