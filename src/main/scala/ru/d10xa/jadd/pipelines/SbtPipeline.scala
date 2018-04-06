package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Utils

import scala.io.Source

class SbtPipeline(ctx: Ctx) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[Artifact] =
      Utils.unshortAll(ctx.config.artifacts.toList, new ArtifactShortcuts().unshort)

    val artifactsWithVersions: Seq[Artifact] = artifacts.map(Utils.loadLatestVersion)

    def toVersionString(a: Artifact): String = {
      val artifactId = a.maybeScalaVersion
        .map { v => a.artifactIdWithScalaVersion(v) }
        .getOrElse(a.artifactId)
      s"""libraryDependencies += "${a.groupId}" % "$artifactId" % "${a.maybeVersion.get}"""" // TODO get
    }

    val artifactStrings = artifactsWithVersions
      .map(toVersionString)
      .toList

    artifactStrings.foreach(println)

    val lines = Source.fromFile(buildFile).getLines().toList
    val newContent =
      new SbtFileInserts()
        .append(lines, artifactStrings)
        .mkString("\n") + "\n"
    if (!ctx.config.dryRun) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }

}

object SbtPipeline {
  def apply(ctx: Ctx): Pipeline = new SbtPipeline(ctx)
}
