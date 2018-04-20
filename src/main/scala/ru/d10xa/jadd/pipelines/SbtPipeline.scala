package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.view.SbtArtifactView

import scala.io.Source

class SbtPipeline(override val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  def makeNewContent(buildFileSource: String, artifacts: Seq[Artifact]): String = {

    val artifactsWithVersions: Seq[Artifact] = artifacts.map(inlineScalaVersion)
    def toVersionStrings(a: Artifact): Seq[String] = new SbtArtifactView(a).showLines
    val artifactStrings: Seq[String] = artifactsWithVersions
      .flatMap(toVersionStrings)
      .toList

    artifactStrings.foreach(println)

    makeNewContentWithStrings(buildFileSource, artifactStrings)
  }

  def makeNewContentWithStrings(buildFileSource: String, artifactStrings: Seq[String]): String = {
    new SbtFileInserts()
      .append(buildFileSource, artifactStrings)
      .mkString("\n") + "\n"
  }

  def buildFileSource: String = Source.fromFile(buildFile).mkString

  def handleArtifacts(artifacts: Seq[Artifact]): Unit = {
    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, makeNewContent(buildFileSource, artifacts))
    }
  }

  override def run(): Unit = {
    val artifacts = loadAllArtifacts()
    handleArtifacts(artifacts.collect { case Right(v) => v })
    handleTroubles(artifacts.collect { case Left(v) => v }, println)
  }

}
