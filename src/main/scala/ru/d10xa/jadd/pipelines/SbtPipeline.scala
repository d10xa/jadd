package ru.d10xa.jadd.pipelines

import better.files._
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.ProjectFileReader
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.ScalaVersion
import ru.d10xa.jadd.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

import scala.util.matching.Regex

class SbtPipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  projectFileReader: ProjectFileReader)
    extends Pipeline
    with StrictLogging {

  val buildFileName = "build.sbt"

  def buildFile[F[_]: Sync]: F[File] =
    projectFileReader.file(buildFileName)

  def buildFileSource[F[_]: Sync]: F[String] =
    buildFile.map(_.contentAsString)

  override def applicable[F[_]: Sync](): F[Boolean] =
    projectFileReader.exists(buildFileName)

  def install[F[_]: Sync](artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource: String = new SbtFileInserts().appendAll(source, artifacts)
      _ <- fileUpdate(newSource)
    } yield ()

  def fileUpdate[F[_]: Sync](str: String): F[Unit] = buildFile.map { f =>
    new SafeFileWriter().write(f, str)
  }

  override def show[F[_]: Sync](): F[Seq[Artifact]] =
    for {
      source <- buildFileSource
      artifacts = new SbtShowCommand(source, projectFileReader, ctx.config)
        .show()
    } yield artifacts

  override def findScalaVersion[F[_]: Sync](): F[Option[ScalaVersion]] =
    buildFile
      .map(_.contentAsString)
      .map(SbtPipeline.extractScalaVersionFromBuildSbt)
}

object SbtPipeline {
  def extractScalaVersionFromBuildSbt(
    buildFileSource: String
  ): Option[ScalaVersion] = {
    val r1 = "scalaVersion\\s+in\\s+ThisBuild\\s*:=\\s*\"(\\d.\\d{1,2})".r
    val r2 = "scalaVersion\\s*:=\\s*\"(\\d.\\d{1,2})".r
    def allMatches(r: Regex): Vector[Regex.Match] =
      r.findAllMatchIn(buildFileSource).toVector

    Seq(r1, r2).map(allMatches).reduce(_ ++ _) match {
      case matches if matches.isEmpty => None
      case matches =>
        matches
          .minBy(_.start)
          .group(1)
          .some
          .map(ScalaVersion.fromString)
    }
  }
}
