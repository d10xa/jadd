package ru.d10xa.jadd.pipelines

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

import scala.io.Source

class SbtPipeline(override val ctx: Ctx, artifactInfoFinder: ArtifactInfoFinder)
    extends Pipeline
    with StrictLogging {

  import ru.d10xa.jadd.implicits.sbt._

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  def buildFileSource: String = Source.fromFile(buildFile).mkString

  def install(artifacts: List[Artifact]): Unit = {

    val artifactStrings: Seq[String] = artifacts
      .flatMap(a => asPrintLines(a) ++ availableVersionsAsPrintLines(a))

    artifactStrings.foreach(s => logger.info(s))

    val newSource: String =
      new SbtFileInserts().appendAll(buildFileSource, artifacts)

    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, newSource)
    }
  }

  override def show(): Unit =
    logger.info(new SbtShowCommand(buildFileSource).show())

}
