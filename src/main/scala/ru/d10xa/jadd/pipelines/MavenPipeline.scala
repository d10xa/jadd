package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.ArtifactWithoutVersion
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

class MavenPipeline(ctx: Ctx) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "pom.xml")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {
    val artifacts: Seq[ArtifactWithoutVersion] =
      Utils.unshortAll(ctx.config.artifacts.toList, new ArtifactShortcuts().unshort)

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)
    val strings = artifactsWithVersions
      .map { a =>
        s"""<dependency>
                     |    <groupId>${a.groupId}</groupId>
                     |    <artifactId>${a.artifactId}</artifactId>
                     |    <version>${a.version}</version>
                     |</dependency>""".stripMargin
      }
      .toList
    strings.foreach(println)
  }

}

object MavenPipeline {
  def apply(ctx: Ctx): Pipeline = new MavenPipeline(ctx)
}
