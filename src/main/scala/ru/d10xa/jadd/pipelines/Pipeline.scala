package ru.d10xa.jadd.pipelines

import cats.implicits._
import cats.data.EitherNel
import cats.data.NonEmptyList
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.versions.VersionTools
import ru.d10xa.jadd.view.ArtifactView

trait Pipeline extends StrictLogging {
  def applicable: Boolean
  def run(artifactInfoFinder: ArtifactInfoFinder): Unit =
    if (ctx.config.command == Show) {
      show()
    } else {
      val unshorted: Seq[Artifact] = Utils
        .unshortAll(ctx.config.artifacts.toList, artifactInfoFinder)
      val loaded: List[Either[NonEmptyList[ArtifactTrouble], Artifact]] =
        loadAllArtifacts(unshorted, VersionTools)
      val a: (List[NonEmptyList[ArtifactTrouble]], List[Artifact]) =
        loaded.separate
      val x = a._1.flatMap(_.toList)
      install(a._2)
      handleTroubles(x, s => logger.info(s))
    }
  def install(artifacts: List[Artifact]): Unit
  def show(): Unit
  def ctx: Ctx
  def needWrite: Boolean = ctx.config.command == Install && !ctx.config.dryRun
  def asPrintLines[A](a: A)(implicit view: ArtifactView[A]): Seq[String] =
    view.showLines(a)
  def availableVersionsAsPrintLines(a: Artifact): Seq[String] =
    "available versions:" :: a.versionsForPrint :: Nil

  def loadAllArtifacts(
    artifacts: Seq[Artifact],
    versionTools: VersionTools
  ): List[Either[NonEmptyList[ArtifactTrouble], Artifact]] =
    artifacts.map { artifact =>
      val artifactWithRepo =
        if (artifact.repository.isDefined) Seq(artifact)
        else
          ctx.config.repositories.map(repo =>
            artifact.copy(repository = Some(repo)))

      val res: Seq[EitherNel[ArtifactTrouble, Artifact]] =
        artifactWithRepo.toStream
          .map(versionTools.loadLatestVersion)

      res
        .find(_.isRight)
        .getOrElse(res.head)
    }.toList
}
