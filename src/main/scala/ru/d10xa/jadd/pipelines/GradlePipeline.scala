package ru.d10xa.jadd.pipelines

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.GradleShowCommand

import scala.io.Source

class GradlePipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  import ru.d10xa.jadd.implicits.gradle._

  lazy val buildFile = new File(ctx.config.projectDir, "build.gradle")

  def buildFileSource: String = Source.fromFile(buildFile).mkString

  override def applicable: Boolean = buildFile.exists()

  override def install(artifacts: List[Artifact]): Unit = {
    val artifactStrings: Seq[String] = artifacts
      .flatMap(a => asPrintLines(a) ++ availableVersionsAsPrintLines(a))

    artifactStrings.foreach(s => logger.info(s))

    val newSource: String = new GradleFileInserts()
      .appendAll(buildFileSource, artifacts)

    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, newSource)
    }
  }

  override def show(): Unit =
    logger.info(new GradleShowCommand(buildFileSource).show())

}
