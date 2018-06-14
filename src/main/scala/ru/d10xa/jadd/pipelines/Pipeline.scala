package ru.d10xa.jadd.pipelines

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.ArtifactTrouble

trait Pipeline {
  def applicable: Boolean
  def run(): Unit = if (ctx.config.command == Show) show() else install()
  def install(): Unit
  def show(): Unit
  def ctx: Ctx
  def needWrite: Boolean = ctx.config.command == Install && !ctx.config.dryRun
  def loadAllArtifacts()(implicit artifactInfoFinder: ArtifactInfoFinder): Seq[Either[ArtifactTrouble, Artifact]] =
    Utils
      .unshortAll(ctx.config.artifacts.toList, artifactInfoFinder)
      .map(Utils.loadLatestVersion)
}
