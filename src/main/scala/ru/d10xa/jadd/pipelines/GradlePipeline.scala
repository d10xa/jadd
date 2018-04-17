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
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.ArtifactTrouble

import scala.io.Source

class GradlePipeline(ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.gradle")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[Artifact] =
      Utils.unshortAll(ctx.config.artifacts.toList, artifactInfoFinder)

    val artifactsWithVersions: Seq[Either[ArtifactTrouble, Artifact]] =
      artifacts.map(Utils.loadLatestVersion)

    def artifactToString(a: Artifact): String =
      if (a.scope.contains(Test)) s"""testCompile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""
      else s"""compile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""

    val strings: Seq[Either[ArtifactTrouble, String]] =
      for {
        a: Either[ArtifactTrouble, Artifact] <- artifactsWithVersions
      } yield a match {
        case Left(trouble) => Left(trouble)
        case Right(artifact) => Right(artifactToString(artifact))
      }

    def readLines = Source.fromFile(buildFile).getLines().toList

    def newContent(lines: List[String], dependencies: List[String]): String =
      new GradleFileInserts()
        .append(lines, dependencies)
        .mkString("\n") + "\n"

    def needWrite = ctx.config.command == Install && !ctx.config.dryRun

    if (needWrite) {
      val c = newContent(readLines, strings.collect { case Right(v) => v }.toList)
      new SafeFileWriter().write(buildFile, c)
    }

  }
}
