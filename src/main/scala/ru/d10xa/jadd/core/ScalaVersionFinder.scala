package ru.d10xa.jadd.core

import better.files._
import cats.Functor
import ru.d10xa.jadd.core.types.ScalaVersion

import scala.util.matching.Regex
import cats.implicits._

trait ScalaVersionFinder[F[_]] {
  def findScalaVersion(): F[Option[ScalaVersion]]
}

class LiveSbtScalaVersionFinder[F[_]: Functor] private (
  ctx: Ctx,
  projectFileReader: ProjectFileReader[F])
    extends ScalaVersionFinder[F] {

  val buildFileName = "build.sbt"
  val buildFile: F[File] = projectFileReader.file(buildFileName)

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    buildFile
      .map(_.contentAsString)
      .map(LiveSbtScalaVersionFinder.extractScalaVersionFromBuildSbt)

}

object LiveSbtScalaVersionFinder {
  def make[F[_]: Functor](
    ctx: Ctx,
    projectFileReader: ProjectFileReader[F]): ScalaVersionFinder[F] =
    new LiveSbtScalaVersionFinder[F](ctx, projectFileReader)

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
