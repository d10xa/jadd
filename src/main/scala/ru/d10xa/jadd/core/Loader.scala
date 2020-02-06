package ru.d10xa.jadd.core

import cats.data.Ior
import cats.data.IorNel
import cats.effect.Sync
import cats.implicits._
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.RepositoryShortcuts
import ru.d10xa.jadd.versions.ArtifactVersionsDownloader
import ru.d10xa.jadd.versions.VersionTools

trait Loader[F[_]] {
  def load(ctx: Ctx): F[IorNel[ArtifactTrouble, List[Artifact]]]
}

class LiveLoader[F[_]: Sync] private (
  artifactInfoFinder: ArtifactInfoFinder,
  repositoryShortcuts: RepositoryShortcuts)
    extends Loader[F] {

  override def load(ctx: Ctx): F[IorNel[ArtifactTrouble, List[Artifact]]] =
    Pipeline
      .extractArtifacts(ctx)
      .map(artifacts => loadByString(ctx, artifacts.toList))

  def loadByString(
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

object LiveLoader {

  def make[F[_]: Sync](
    artifactInfoFinder: ArtifactInfoFinder,
    repositoryShortcuts: RepositoryShortcuts
  ): Loader[F] = new LiveLoader[F](artifactInfoFinder, repositoryShortcuts)

}
