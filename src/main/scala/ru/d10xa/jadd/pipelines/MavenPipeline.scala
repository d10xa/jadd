package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Cli.Install
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Indent
import ru.d10xa.jadd.Indentation
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.inserts.MavenFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder

import scala.io.Source

class MavenPipeline(ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "pom.xml")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[Artifact] =
      Utils.unshortAll(ctx.config.artifacts.toList, artifactInfoFinder)

    val lines: Seq[String] = Source.fromFile(buildFile).getLines().toSeq
    val indent @ Indent(spaceOrTabChar, count) = Indentation.predictIndentation(lines)
    val indentString = spaceOrTabChar.toString * count

    // TODO reduce copy/paste
    def inlineScalaVersion(artifact: Artifact): Artifact = {
      artifact.maybeScalaVersion.map {v =>
        artifact.copy(artifactId = artifact.artifactIdWithScalaVersion(v))
      }.getOrElse(artifact)
    }

    val artifactsWithVersions: Seq[Artifact] =
      artifacts
        .map(Utils.loadLatestVersion)
        .map(inlineScalaVersion)

    // TODO fix maybeVersion.get
    val strings = artifactsWithVersions
      .map {
        case a if a.scope.contains(Test) =>
          s"""<dependency>
             |$indentString<groupId>${a.groupId}</groupId>
             |$indentString<artifactId>${a.artifactId}</artifactId>
             |$indentString<version>${a.maybeVersion.get}</version>
             |$indentString<scope>test</scope>
             |</dependency>""".stripMargin
        case a =>
          s"""<dependency>
             |$indentString<groupId>${a.groupId}</groupId>
             |$indentString<artifactId>${a.artifactId}</artifactId>
             |$indentString<version>${a.maybeVersion.get}</version>
             |</dependency>""".stripMargin
      }

    strings.foreach(println)

    val newContent =
      MavenFileInserts
        .append(
          lines,
          strings.map(_.split('\n').toSeq),
          indent
        )
        .mkString("\n") + "\n"
    if (ctx.config.command == Install && !ctx.config.dryRun) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }

}
