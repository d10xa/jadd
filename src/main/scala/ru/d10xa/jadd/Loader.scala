package ru.d10xa.jadd

import cats.data.NonEmptyList
import cats.effect.Sync
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.RepositoryShortcuts
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.versions.ArtifactVersionsDownloader
import ru.d10xa.jadd.versions.VersionTools
import cats.implicits._
import ru.d10xa.jadd.Loader.Result

trait Loader {
  def load[F[_]: Sync](ctx: Ctx): F[Result]
}

object Loader {
  class LoaderImpl(
    artifactInfoFinder: ArtifactInfoFinder,
    repositoryShortcuts: RepositoryShortcuts
  ) extends Loader {

    override def load[F[_]: Sync](ctx: Ctx): F[Result] =
      Sync[F].delay {
        val artifacts = Pipeline.extractArtifacts(ctx)
        val unshorted: Seq[Artifact] = Utils
          .unshortAll(artifacts.toList, artifactInfoFinder)
        val repositoriesUnshorted: Seq[String] =
          ctx.config.repositories.map(repositoryShortcuts.unshortRepository)
        val loaded: List[Either[NonEmptyList[ArtifactTrouble], Artifact]] =
          loadAllArtifacts(unshorted, VersionTools, repositoriesUnshorted)
        val (a, b) = loaded.separate
        Result(a, b)
      }

    def loadAllArtifacts(
      artifacts: Seq[Artifact],
      versionTools: VersionTools,
      repositories: Seq[String]
    ): List[Either[NonEmptyList[ArtifactTrouble], Artifact]] =
      artifacts
        .map(
          ArtifactVersionsDownloader
            .loadArtifactVersions(_, repositories, versionTools))
        .toList
  }

  final case class Result(
    errors: List[NonEmptyList[ArtifactTrouble]],
    values: List[Artifact]
  )

}
