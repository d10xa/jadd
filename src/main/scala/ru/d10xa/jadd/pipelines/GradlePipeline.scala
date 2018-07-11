package ru.d10xa.jadd.pipelines

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.GradleShowCommand
import ru.d10xa.jadd.troubles._
import ru.d10xa.jadd.versions.VersionTools
import ru.d10xa.jadd.view.ArtifactView

import scala.io.Source

class GradlePipeline(override val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder)
  extends Pipeline
  with StrictLogging {

  import ArtifactView._
  import ru.d10xa.jadd.implicits.gradle._

  lazy val buildFile = new File(ctx.config.projectDir, "build.gradle")

  def buildFileSource: String = Source.fromFile(buildFile).mkString

  override def applicable: Boolean = buildFile.exists()

  override def install(): Unit = {
    val artifacts = loadAllArtifacts(VersionTools)
    handleArtifacts(artifacts.collect { case Right(v) => v })
    handleTroubles(artifacts.collect { case Left(v) => v }, println)
  }

  def handleArtifacts(artifacts: Seq[Artifact]): Unit = {
    val artifactStrings: Seq[String] = artifacts
      .flatMap(_.showLines)
      .toList

    artifactStrings.foreach(println)

    val newSource: String = new GradleFileInserts()
      .appendAll(buildFileSource, artifacts)

    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, newSource)
    }
  }

  override def show(): Unit = {
    logger.info(new GradleShowCommand(buildFileSource).show())
  }

}
