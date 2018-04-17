package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Cli.Install
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.Utils

import scala.io.Source

class SbtPipeline(ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[Artifact] =
      Utils.unshortAll(ctx.config.artifacts.toList, artifactInfoFinder)

    val artifactsWithVersions: Seq[Artifact] =
      artifacts.map(Utils.loadLatestVersion)
        .collect { case Right(v) => v } // TODO refactoring

    def toVersionString(a: Artifact): String = {
      val artifactId = a.maybeScalaVersion
        .map { v => a.artifactIdWithScalaVersion(v) }
        .getOrElse(a.artifactId)
      val groupId = a.groupId
      val version = a.maybeVersion.get // TODO get
      a.scope match {
        case Some(Test) =>
          s"""libraryDependencies += "$groupId" % "$artifactId" % "$version" % Test"""
        case _ =>
          s"""libraryDependencies += "$groupId" % "$artifactId" % "$version""""
      }
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
    if (ctx.config.command == Install && !ctx.config.dryRun) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }

}
