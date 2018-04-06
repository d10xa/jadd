package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
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
    val artifacts: Seq[Artifact] =
      Utils.unshortAll(ctx.config.artifacts.toList, new ArtifactShortcuts().unshort)

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)
    val strings = artifactsWithVersions
      .map { a => s"""compile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}"""" } // TODO get
      .toList

    strings.foreach(println)

    val lines = Source.fromFile(buildFile).getLines().toList

    val newContent =
      new GradleFileInserts()
        .append(lines, strings)
        .mkString("\n") + "\n"
    if (!ctx.config.dryRun) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }
}

object GradlePipeline {
  def apply(ctx: Ctx): Pipeline = new GradlePipeline(ctx)
}
