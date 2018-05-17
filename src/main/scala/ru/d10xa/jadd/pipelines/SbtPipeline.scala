package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.inserts.SbtArtifactMatcher
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.view.ArtifactView
import ru.d10xa.jadd.view.ArtifactView.Match

import scala.io.Source

class SbtPipeline(override val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder) extends Pipeline {

  import ArtifactView._
  import SbtPipeline._

  lazy val buildFile = new File(ctx.config.projectDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  def buildFileSource: String = Source.fromFile(buildFile).mkString

  def handleArtifacts(artifacts: Seq[Artifact]): Unit = {

    val artifactStrings: Seq[String] = artifacts
      .flatMap(_.showLines)
      .toList

    artifactStrings.foreach(println)

    val str: String = new SbtFileInserts()
      .append(buildFileSource, artifacts)

    if (this.needWrite) {
      new SafeFileWriter().write(buildFile, str)
    }
  }

  override def run(): Unit = {
    val artifacts = loadAllArtifacts()
    handleArtifacts(artifacts.collect { case Right(v) => v })
    handleTroubles(artifacts.collect { case Left(v) => v }, println)
  }

}

object SbtPipeline {

  implicit val sbtArtifactView: ArtifactView[Artifact] = new ArtifactView[Artifact]{
    override def showLines(artifact: Artifact): Seq[String] = {
      val groupId = artifact.groupId
      val version = artifact.maybeVersion.get // TODO get

      val groupAndArtifact = (artifact.explicitScalaVersion, artifact.maybeScalaVersion) match {
        case (true, Some(scalaVersion)) if artifact.isScala =>
          s""""$groupId" % "${artifact.artifactIdWithScalaVersion(scalaVersion)}""""
        case (false, Some(_)) =>
          s""""$groupId" %% "${artifact.artifactIdWithoutScalaVersion}""""
        case (_, None) =>
          s""""$groupId" % "${artifact.artifactId}""""
      }

      val prefix = if(artifact.inSequence) "" else "libraryDependencies += "

      artifact.scope match {
        case Some(Test) =>
          s"""$prefix$groupAndArtifact % "$version" % Test""" :: Nil
        case _ =>
          s"""$prefix$groupAndArtifact % "$version"""" :: Nil
      }
    }

    override def find(artifact: Artifact, source: String): Seq[Match] =
      new SbtArtifactMatcher(source).find(artifact)
  }

}
