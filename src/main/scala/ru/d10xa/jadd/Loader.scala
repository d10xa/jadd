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

    def loadByString[F[_]: Sync](
      ctx: Ctx,
      artifacts: List[String]): IorNel[ArtifactTrouble, List[Artifact]] = {
      val withScalaVersion: Seq[Artifact] => Seq[Artifact] = _.map(
        u =>
          if (u.isScala)
            u.copy(
              maybeScalaVersion =
                u.maybeScalaVersion.orElse(ctx.meta.scalaVersion))
          else u
      )
      val unshorted: Seq[Artifact] = Utils
        .unshortAll(artifacts, artifactInfoFinder)
      val repositoriesUnshorted: Seq[String] =
        ctx.config.repositories.map(repositoryShortcuts.unshortRepository)
      loadAllArtifacts(
        withScalaVersion(unshorted),
        VersionTools,
        repositoriesUnshorted)
    }

    override def load[F[_]: Sync](
      ctx: Ctx): F[IorNel[ArtifactTrouble, List[Artifact]]] =
      Pipeline
        .extractArtifacts(ctx)
        .map(artifacts => loadByString(ctx, artifacts.toList))

    def loadAllArtifacts(
      artifacts: Seq[Artifact],
      versionTools: VersionTools,
      repositories: Seq[String]
    ): IorNel[ArtifactTrouble, List[Artifact]] = {

      val initial: IorNel[ArtifactTrouble, List[Artifact]] = Ior.Right(List())

      artifacts
        .map(ArtifactVersionsDownloader
          .loadArtifactVersions(_, repositories, versionTools))
        .map(_.map(List(_)))
        .foldLeft(initial)((a, b) => a.combine(b))
    }

  }

  final case class Result(
    errors: List[NonEmptyList[ArtifactTrouble]],
    values: List[Artifact]
  )

}
