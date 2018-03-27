package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.ArtifactWithoutVersion
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import scala.io.Source

class GradlePipeline(ctx: Ctx) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.gradle")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[ArtifactWithoutVersion] =
      Utils.unshortAll(ctx.config.artifacts.toList, new ArtifactShortcuts().unshort)

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)
    val strings = artifactsWithVersions
      .map { a => s"""compile "${a.groupId}:${a.artifactId}:${a.version}"""" }
      .toList

    val lines = Source.fromFile(buildFile).getLines().toList
    val newContent =
      new GradleFileInserts()
        .append(lines, strings)
        .mkString("\n") + "\n"
    new SafeFileWriter().write(buildFile, newContent)
  }
}

object GradlePipeline {
  def apply(ctx: Ctx): Pipeline = new GradlePipeline(ctx)
}
