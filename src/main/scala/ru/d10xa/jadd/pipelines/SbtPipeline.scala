package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.ArtifactWithoutVersion
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import scala.io.Source

class SbtPipeline(ctx: Ctx) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[ArtifactWithoutVersion] =
      Utils.unshortAll(ctx.config.artifacts.toList, new ArtifactShortcuts().unshort)

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)
    val strings = artifactsWithVersions
      .map { a => s"""libraryDependencies += "${a.groupId}" % "${a.artifactId}" % "${a.version}"""" }
      .toList

    strings.foreach(println)

    val lines = Source.fromFile(buildFile).getLines().toList
    val newContent =
      new SbtFileInserts()
        .append(lines, strings)
        .mkString("\n") + "\n"
    if (!ctx.config.dryRun) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }

}

object SbtPipeline {
  def apply(ctx: Ctx): Pipeline = new SbtPipeline(ctx)
}
