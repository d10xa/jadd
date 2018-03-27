package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.ArtifactWithoutVersion
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Indentation
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.inserts.MavenFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import scala.io.Source

class MavenPipeline(ctx: Ctx) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "pom.xml")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[ArtifactWithoutVersion] =
      Utils.unshortAll(ctx.config.artifacts.toList, new ArtifactShortcuts().unshort)

    val lines = Source.fromFile(buildFile).getLines().toList
    val (spaceOrTabChar, count) = Indentation.predictIndentation(lines)
    val indentString = spaceOrTabChar.toString * count

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)
    val strings = artifactsWithVersions
      .map { a =>
        s"""<dependency>
        |${indentString}<groupId>${a.groupId}</groupId>
        |${indentString}<artifactId>${a.artifactId}</artifactId>
        |${indentString}<version>${a.version}</version>
        |</dependency>""".stripMargin
      }
      .toList
    strings.foreach(println)
    val newContent =
      MavenFileInserts
        .append(lines, strings.map(_.split('\n').toList))
        .mkString("\n") + "\n"
    new SafeFileWriter().write(buildFile, newContent)
  }

}

object MavenPipeline {
  def apply(ctx: Ctx): Pipeline = new MavenPipeline(ctx)
}
