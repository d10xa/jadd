package ru.d10xa.jadd

import cats.data.Ior
import cats.data.IorNel
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.RepositoryShortcuts
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.versions.ArtifactVersionsDownloader
import ru.d10xa.jadd.versions.VersionTools

trait Loader {
  def load[F[_]: Sync](ctx: Ctx): F[IorNel[ArtifactTrouble, List[Artifact]]]
}

object Loader {
  class LoaderImpl(
    artifactInfoFinder: ArtifactInfoFinder,
    repositoryShortcuts: RepositoryShortcuts
  ) extends Loader {

    override def load[F[_]: Sync](
      ctx: Ctx): F[IorNel[ArtifactTrouble, List[Artifact]]] =
      Sync[F].delay {
        val artifacts = Pipeline.extractArtifacts(ctx)
        val unshorted: Seq[Artifact] = Utils
          .unshortAll(artifacts.toList, artifactInfoFinder)
        val withScalaVersion = unshorted.map(
          u =>
            if (u.isScala)
              u.copy(
                maybeScalaVersion =
                  u.maybeScalaVersion.orElse(ctx.meta.scalaVersion))
            else u
        )
        val repositoriesUnshorted: Seq[String] =
          ctx.config.repositories.map(repositoryShortcuts.unshortRepository)
        loadAllArtifacts(withScalaVersion, VersionTools, repositoriesUnshorted)
      }

    def loadAllArtifacts(
      artifacts: Seq[Artifact],
      versionTools: VersionTools,
      repositories: Seq[String]
    ): IorNel[ArtifactTrouble, List[Artifact]] = {

      def loadVersions(a: Artifact): IorNel[ArtifactTrouble, Artifact] =
        ArtifactVersionsDownloader
          .loadArtifactVersions(a, repositories, versionTools)

      val initial: IorNel[ArtifactTrouble, List[Artifact]] = Ior.Right(List())

      artifacts
        .map(loadVersions)
        .map(_.map(List(_)))
        .foldLeft(initial)((a, b) => a.combine(b))
    }

  }

  final case class Result(
    errors: List[NonEmptyList[ArtifactTrouble]],
    values: List[Artifact]
  )

}
