package ru.d10xa.jadd.versions

import cats.syntax.all._
import cats.data.EitherNel
import cats.effect.Sync
import coursier.ModuleName
import coursier.Organization
import coursier.core.Module
import coursier.core.Repository
import coursier.core.Version
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import ru.d10xa.jadd.coursier_.CoursierVersions

// TODO rename
trait VersionTools[F[_]] {

  def loadVersionAndInitLatest(
    artifact: Artifact,
    repositories: Seq[Repository]
  ): F[EitherNel[ArtifactTrouble, Artifact]]
}

object VersionTools {

  def make[F[_]: Sync](
    coursierVersions: CoursierVersions[F]
  ): VersionTools[F] = new VersionTools[F] {

    private def loadVersions(
      artifact: Artifact,
      repositories: Seq[Repository]
    ): F[EitherNel[ArtifactTrouble, Artifact]] = {

      val module: Module =
        coursier.core.Module(
          Organization(artifact.groupId.value),
          ModuleName(artifact.inlineScalaVersion.artifactId),
          Map.empty
        )

      for {
        versions <- coursierVersions.versions(repositories, module)
        res = artifact
          .copy(
            availableVersions =
              versions.available.map(Version(_)).sorted.reverse
          )
          .asRight
      } yield res

    }

    private def initLatestVersion(
      artifact: Artifact,
      versionFilter: VersionFilter = VersionFilter
    ): Artifact =
      artifact.copy(maybeVersion =
        versionFilter.excludeNonRelease(artifact.availableVersions).headOption
      )

    override def loadVersionAndInitLatest(
      artifact: Artifact,
      repositories: Seq[Repository]
    ): F[EitherNel[ArtifactTrouble, Artifact]] =
      loadVersions(artifact, repositories).map(
        _.map(a => initLatestVersion(a, VersionFilter))
      )

  }
}
