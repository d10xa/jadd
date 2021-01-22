package ru.d10xa.jadd.core

import cats.syntax.all._
import cats.Functor
import cats.data.Ior
import cats.data.IorNel
import cats.effect.Sync
import coursier.parse.RepositoryParser
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.versions.ArtifactVersionsDownloader
import ru.d10xa.jadd.versions.VersionTools

trait Loader[F[_]] {
  def load(
    ctx: Ctx,
    versionTools: VersionTools[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[IorNel[ArtifactTrouble, List[Artifact]]]
}

class LiveLoader[F[_]: Sync] private (
  artifactInfoFinder: ArtifactInfoFinder[F]
) extends Loader[F] {

  override def load(
    ctx: Ctx,
    versionTools: VersionTools[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[IorNel[ArtifactTrouble, List[Artifact]]] =
    Pipeline
      .extractArtifacts(ctx)
      .flatMap(artifacts =>
        loadByString(ctx, artifacts.toList, versionTools, artifactShortcuts)
      )

  def withScalaVersion[M[_]: Functor](
    ctx: Ctx,
    artifacts: M[Artifact]
  ): M[Artifact] =
    artifacts.map(u =>
      if (u.isScala)
        u.copy(
          maybeScalaVersion = u.maybeScalaVersion.orElse(ctx.meta.scalaVersion)
        )
      else u
    )

  def loadByString(
    ctx: Ctx,
    artifacts: List[String],
    versionTools: VersionTools[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[IorNel[ArtifactTrouble, List[Artifact]]] =
    for {
      unshorted <- Utils
        .unshortAll(artifacts, artifactInfoFinder, artifactShortcuts)
      repositoriesParsed <- RepositoryParser
        .repositories(ctx.config.repositories)
        .either match {
        case Left(errs) =>
          Sync[F].raiseError(
            new RuntimeException(
              s"Error parsing repositories:" + System.lineSeparator() +
                errs.map("  " + _ + System.lineSeparator()).mkString
            )
          )
        case Right(value) => Sync[F].pure(value.map(_.repr))
      }
      result <- loadAllArtifacts(
        withScalaVersion(ctx, unshorted),
        versionTools,
        repositoriesParsed
      )
    } yield result

  def loadAllArtifacts(
    artifacts: Seq[Artifact],
    versionTools: VersionTools[F],
    repositories: Seq[String]
  ): F[IorNel[ArtifactTrouble, List[Artifact]]] = {

    val initial: IorNel[ArtifactTrouble, List[Artifact]] = Ior.Right(List())

    artifacts
      .traverse(
        ArtifactVersionsDownloader
          .loadArtifactVersions(_, repositories, versionTools)
      )
      .map(
        _.map(_.map(List(_)))
          .foldLeft(initial)((a, b) => a.combine(b))
      )
  }

}

object LiveLoader {

  def make[F[_]: Sync](
    artifactInfoFinder: ArtifactInfoFinder[F]
  ): Loader[F] = new LiveLoader[F](artifactInfoFinder)

}
