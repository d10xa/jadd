package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.handleTroubles
import ru.d10xa.jadd.view.ArtifactView

import scala.io.Source
import scala.util.matching.Regex

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

      artifact.scope match {
        case Some(Test) =>
          s"""libraryDependencies += $groupAndArtifact % "$version" % Test""" :: Nil
        case _ =>
          s"""libraryDependencies += $groupAndArtifact % "$version"""" :: Nil
      }
    }
{}
    override def find(artifact: Artifact, source: String): Option[String] = {
      // TODO Add dependencies to sequence if present (sbt) #14
      val groupId = artifact.groupId

      def regex0: Option[Regex] = Some(
        raw"""libraryDependencies\s\+=\s["']$groupId["']\s%\s["']${artifact.artifactId}["']\s%\s["'].+["'](\s%\sTest)?""".r
      )

      def regex1: Option[Regex] = Some(
        raw"""libraryDependencies\s\+=\s["']$groupId["']\s%%\s["']${artifact.artifactIdWithoutScalaVersion}["']\s%\s["'].+["'](\s%\sTest)?""".r
      )

      def regex2: Option[Regex] = (artifact.isScala, artifact.maybeScalaVersion) match {
        case (true, Some(scalaVersion)) =>
          val aId = artifact.artifactIdWithScalaVersion(scalaVersion)
          Some(raw"""libraryDependencies\s\+=\s["']$groupId["']\s%\s["']$aId["']\s%\s["'].+["'](\s%\sTest)?""".r)
        case _ => None
      }

      def findMaybeMatch(seq: Option[Regex]*): Option[String] =
        seq.flatMap(_.map(_.findFirstIn(source))).reduce(_ orElse _)

      findMaybeMatch(regex0, regex1, regex2)

    }
  }

}
