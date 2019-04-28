package ru.d10xa.jadd.pipelines

import cats.data.NonEmptyList
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.RepositoryShortcuts
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.versions.ArtifactVersionsDownloader
import ru.d10xa.jadd.versions.VersionTools
import ru.d10xa.jadd.view.ArtifactView

trait Pipeline extends StrictLogging {
  def applicable: Boolean
  def run(
    artifactInfoFinder: ArtifactInfoFinder,
    repositoryShortcuts: RepositoryShortcuts
  ): Unit =
    if (ctx.config.command == Show) {
      logger.info(ctx.config.showPrinter.mkString(show().toList))
    } else {
      val artifacts = Pipeline.extractArtifacts(ctx)
      val unshorted: Seq[Artifact] = Utils
        .unshortAll(artifacts.toList, artifactInfoFinder)
      val repositoriesUnshorted: Seq[String] =
        ctx.config.repositories.map(repositoryShortcuts.unshortRepository)
      val loaded: List[Either[NonEmptyList[ArtifactTrouble], Artifact]] =
        loadAllArtifacts(unshorted, VersionTools, repositoriesUnshorted)
      val a: (List[NonEmptyList[ArtifactTrouble]], List[Artifact]) =
        loaded.separate
      val x = a._1.flatMap(_.toList)
      install(a._2)
      handleTroubles(x, s => logger.info(s))
    }
  def install(artifacts: List[Artifact]): Unit
  def show(): Seq[Artifact]
  def ctx: Ctx
  def needWrite: Boolean = ctx.config.command == Install && !ctx.config.dryRun
  def asPrintLines[A](a: A)(implicit view: ArtifactView[A]): Seq[String] =
    view.showLines(a)
  def availableVersionsAsPrintLines(a: Artifact): Seq[String] =
    "available versions:" :: a.versionsForPrint :: Nil

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

object Pipeline {

  def extractArtifacts(ctx: Ctx): Seq[String] =
    if (ctx.config.requirements.nonEmpty) {
      for {
        requirement <- ctx.config.requirements
        source = Utils.mkStringFromResource(requirement)
        artifact <- source.trim.split("\\r?\\n").map(_.trim)
      } yield artifact
    } else {
      ctx.config.artifacts
    }

}
