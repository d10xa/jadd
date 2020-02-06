package ru.d10xa.jadd.pipelines

import better.files._
import cats.data.Chain
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.ProjectFileReader
import ru.d10xa.jadd.core.SafeFileWriter
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.SbtFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

import scala.util.matching.Regex

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  projectFileReader: ProjectFileReader)
    extends Pipeline[F]
    with StrictLogging {

  val buildFileName = "build.sbt"

  def buildFile: F[File] =
    projectFileReader.file(buildFileName)

  def buildFileSource: F[String] =
    buildFile.map(_.contentAsString)

  override def applicable(): F[Boolean] =
    projectFileReader.exists(buildFileName)

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource: String = new SbtFileInserts().appendAll(source, artifacts)
      _ <- fileUpdate(newSource)
    } yield ()

  def fileUpdate(str: String): F[Unit] = buildFile.map { f =>
    new SafeFileWriter().write(f, str)
  }

  override def show(): F[Chain[Artifact]] =
    for {
      source <- buildFileSource
      artifacts = new SbtShowCommand(source, projectFileReader, ctx.config)
        .show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
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
