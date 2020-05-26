package ru.d10xa.jadd.core

import java.nio.file.Path
import java.nio.file.Paths

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.core.types.ApplicativeThrowable
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps

import scala.util.matching.Regex

trait ScalaVersionFinder[F[_]] {
  def findScalaVersion(): F[Option[ScalaVersion]]
}

class LiveSbtScalaVersionFinder[F[_]: Sync] private (
  ctx: Ctx,
  fileOps: FileOps[F])
    extends ScalaVersionFinder[F] {

  val buildFilePath: Path = Paths.get("build.sbt")

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    for {
      fsItem <- fileOps.read(buildFilePath)
      optScalaVersion <- fsItem match {
        case TextFile(content) =>
          Applicative[F]
            .pure(
              LiveSbtScalaVersionFinder.extractScalaVersionFromBuildSbt(
                content))
        case _ =>
          ApplicativeThrowable[F]
            .raiseError[Option[ScalaVersion]](
              new IllegalStateException("can not read build.sbt"))
      }
    } yield optScalaVersion

}

object LiveSbtScalaVersionFinder {
  def make[F[_]: Sync](ctx: Ctx, fileOps: FileOps[F]): ScalaVersionFinder[F] =
    new LiveSbtScalaVersionFinder[F](ctx, fileOps)

  def extractScalaVersionFromBuildSbt(
    buildFileSource: FileContent
  ): Option[ScalaVersion] = {
    val r1 = "scalaVersion\\s+in\\s+ThisBuild\\s*:=\\s*\"(\\d.\\d{1,2})".r
    val r2 = "scalaVersion\\s*:=\\s*\"(\\d.\\d{1,2})".r
    def allMatches(r: Regex): Vector[Regex.Match] =
      r.findAllMatchIn(buildFileSource.value).toVector

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
