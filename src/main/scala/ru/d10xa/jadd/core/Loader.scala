package ru.d10xa.jadd.core

import cats.Functor
import cats.data.Ior
import cats.data.IorNel
import cats.effect.Sync
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
  repositoryShortcuts: RepositoryShortcuts,
  versionTools: VersionTools = VersionTools)
    extends Loader[F] {

  override def load(ctx: Ctx): F[IorNel[ArtifactTrouble, List[Artifact]]] =
    Pipeline
      .extractArtifacts(ctx)
      .flatMap(artifacts => loadByString(ctx, artifacts.toList))

  def withScalaVersion[M[_]: Functor](
    ctx: Ctx,
    artifacts: M[Artifact]
  ): M[Artifact] =
    artifacts.map(
      u =>
        if (u.isScala)
          u.copy(
            maybeScalaVersion =
              u.maybeScalaVersion.orElse(ctx.meta.scalaVersion))
        else u
    )

  def loadByString(
    ctx: Ctx,
    artifacts: List[String]
  ): F[IorNel[ArtifactTrouble, List[Artifact]]] =
    for {
      unshorted <- Utils
        .unshortAll(artifacts, artifactInfoFinder)
      repositoriesUnshorted = ctx.config.repositories
        .map(repositoryShortcuts.unshortRepository)
        .toList
      x = loadAllArtifacts(
        withScalaVersion(ctx, unshorted),
        versionTools,
        repositoriesUnshorted)
    } yield x

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
