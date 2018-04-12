package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Cli.Install
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder

import scala.io.Source

class GradlePipeline(ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.gradle")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[Artifact] =
      Utils.unshortAll(ctx.config.artifacts.toList, artifactInfoFinder)

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)
    val strings = artifactsWithVersions
      .map {
        case a if a.scope.contains(Test) => s"""testCompile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""
        case a => s"""compile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""
      } // TODO get
      .toList

    strings.foreach(println)

    val lines = Source.fromFile(buildFile).getLines().toList

    val newContent =
      new GradleFileInserts()
        .append(lines, strings)
        .mkString("\n") + "\n"
    if (ctx.config.command == Install && !ctx.config.dryRun) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }
}
