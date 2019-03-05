package ru.d10xa.jadd.pipelines

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Indentation
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.MavenFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.MavenShowCommand

import scala.io.Source

class MavenPipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  import ru.d10xa.jadd.implicits.maven._

  lazy val buildFile = new File(ctx.config.projectDir, "pom.xml")

  lazy val buildFileSource: String = Source.fromFile(buildFile).mkString

  override def applicable: Boolean = buildFile.exists()

  override def install(artifacts: List[Artifact]): Unit = {

    val indent = Indentation.predictIndentation(buildFileSource.split('\n'))

    val artifactsWithVersions = artifacts.map(_.inlineScalaVersion)

    // TODO stringsForPrint, stringsForInsert refactoring
    val stringsForPrint = artifactsWithVersions
      .map(_ -> indent)
      .flatMap {
        case t @ (artifact, _) =>
          asPrintLines(t) ++ availableVersionsAsPrintLines(artifact)
      }

    val stringsForInsert = artifactsWithVersions
      .map(_ -> indent)
      .flatMap(asPrintLines(_))

    stringsForPrint.foreach(s => logger.info(s))

    def newContent =
      MavenFileInserts
        .append(
          buildFileSource,
          stringsForInsert.map(_.split('\n').toSeq),
          indent
        )
        .mkString("\n") + "\n"
    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }

  override def show(): Unit =
    logger.info(new MavenShowCommand(buildFileSource).show())

}
