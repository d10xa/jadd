package ru.d10xa.jadd.pipelines

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Indent
import ru.d10xa.jadd.Indentation
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.inserts.MavenFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles._
import ru.d10xa.jadd.versions.VersionTools

import scala.io.Source

class MavenPipeline(override val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder)
  extends Pipeline
  with StrictLogging {

  lazy val buildFile = new File(ctx.config.projectDir, "pom.xml")

  lazy val buildFileSource: String = Source.fromFile(buildFile).mkString

  override def applicable: Boolean = buildFile.exists()

  override def install(): Unit = {

    val indent @ Indent(spaceOrTabChar, count) = Indentation.predictIndentation(buildFileSource.split('\n'))
    val indentString = spaceOrTabChar.toString * count

    val artifactsWithVersions: Seq[Either[ArtifactTrouble, Artifact]] =
      loadAllArtifacts(VersionTools)
        .map(_.map(_.inlineScalaVersion))

    // TODO fix maybeVersion.get
    val strings = artifactsWithVersions.collect { case Right(v) => v } // TODO refactoring

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
    handleTroubles(artifactsWithVersions.collect { case Left(trouble) => trouble }, println)

    def newContent =
      MavenFileInserts
        .append(
          buildFileSource,
          strings.map(_.split('\n').toSeq),
          indent
        )
        .mkString("\n") + "\n"
    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, newContent)
    }
  }

  override def show(): Unit = {
    logger.info(buildFileSource)
  }

}
