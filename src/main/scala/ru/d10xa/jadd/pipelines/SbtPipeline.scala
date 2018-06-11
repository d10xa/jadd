package ru.d10xa.jadd.pipelines

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.view.ArtifactView

import scala.io.Source

class SbtPipeline(override val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder)
  extends Pipeline
  with StrictLogging {

  import ArtifactView._
  import ru.d10xa.jadd.implicits.sbt._

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  def buildFileSource: String = Source.fromFile(buildFile).mkString

  def handleArtifacts(artifacts: Seq[Artifact]): Unit = {

    val artifactStrings: Seq[String] = artifacts
      .flatMap(_.showLines)
      .toList

    artifactStrings.foreach(println)

    val newSource: String = new SbtFileInserts().appendAll(buildFileSource, artifacts)

    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, newSource)
    }
  }

  override def install(): Unit = {
    val artifacts = loadAllArtifacts()
    handleArtifacts(artifacts.collect { case Right(v) => v })
    handleTroubles(artifacts.collect { case Left(v) => v }, println)
  }

  override def show(): Unit = {
    logger.info(new SbtShowCommand(buildFileSource).show())
  }

}
