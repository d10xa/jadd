package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.ArtifactTrouble

import scala.io.Source

class GradlePipeline(override val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "build.gradle")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {

    def artifactToString(a: Artifact): String =
      if (a.scope.contains(Test)) s"""testCompile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""
      else s"""compile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""

    val allArtifacts: Seq[Either[ArtifactTrouble, Artifact]] =
      loadAllArtifacts()
        .map(_.map(inlineScalaVersion))

    val strings: Seq[Either[ArtifactTrouble, String]] =
      for {
        a: Either[ArtifactTrouble, Artifact] <- allArtifacts
      } yield a match {
        case Left(trouble) => Left(trouble)
        case Right(artifact) => Right(artifactToString(artifact))
      }

    val source = Source.fromFile(buildFile).mkString

    def newContent(source: String, dependencies: List[String]): String =
      new GradleFileInserts()
        .append(source, dependencies)
        .mkString("\n") + "\n"

    if (this.needWrite) {
      val c = newContent(source, strings.collect { case Right(v) => v }.toList)
      new SafeFileWriter().write(buildFile, c)
    }

  }

}
