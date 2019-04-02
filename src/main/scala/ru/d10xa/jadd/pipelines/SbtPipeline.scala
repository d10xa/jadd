package ru.d10xa.jadd.pipelines

import java.io.File

import cats.effect.SyncIO
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.ProjectFileReader
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

class SbtPipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  projectFileReader: ProjectFileReader)
    extends Pipeline
    with StrictLogging {

  val buildFileName = "build.sbt"

  import ru.d10xa.jadd.implicits.sbt._

  val buildFile: SyncIO[File] =
    projectFileReader.file(buildFileName)

  override def applicable: Boolean =
    projectFileReader.exists(buildFileName).unsafeRunSync()

  def buildFileSource: String =
    projectFileReader.read(buildFileName).unsafeRunSync()

  def install(artifacts: List[Artifact]): Unit = {

    val artifactStrings: Seq[String] = artifacts
      .flatMap(a => asPrintLines(a) ++ availableVersionsAsPrintLines(a))

    artifactStrings.foreach(s => logger.info(s))

    val newSource: String =
      new SbtFileInserts().appendAll(buildFileSource, artifacts)

    val fileUpdate = buildFile.map { f =>
      new SafeFileWriter().write(f, newSource)
    }

    if (this.needWrite) {
      fileUpdate.unsafeRunSync()
    }
  }

  override def show(): Seq[Artifact] =
    new SbtShowCommand(buildFileSource, projectFileReader).show()

}
