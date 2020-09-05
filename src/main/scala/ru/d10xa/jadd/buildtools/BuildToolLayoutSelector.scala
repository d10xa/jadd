package ru.d10xa.jadd.buildtools

import java.nio.file.Path
import java.nio.file.Paths

import cats.effect.Sync
import cats.syntax.all._
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.buildtools.BuildToolLayout._
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem.Dir
import ru.d10xa.jadd.fs.FsItem.FileNotFound
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.instances._

trait BuildToolLayoutSelector[F[_]] {
  def select(ctx: Ctx): F[BuildToolLayout]
}

object BuildToolLayoutSelector {
  def make[F[_]: Sync](
    fileOps: FileOps[F]
  ): BuildToolLayoutSelector[F] =
    new BuildToolLayoutSelector[F] {

      private def fromPath(p: Path): Option[BuildToolLayout] =
        if (p.show.endsWith(".sc")) {
          Ammonite.some
        } else {
          none[BuildToolLayout]
        }
      private def fromDir(names: Set[Path]): Option[BuildToolLayout] = {
        val namesStr = names.map(_.getFileName.show)
        def sbt = namesStr.contains("build.sbt")
        def maven = namesStr.contains("pom.xml")
        def gradle = namesStr.contains("build.gradle")
        if (maven) {
          Maven.some
        } else if (sbt) {
          Sbt.some
        } else if (gradle) {
          Gradle.some
        } else {
          none[BuildToolLayout]
        }
      }

      override def select(ctx: Ctx): F[BuildToolLayout] = {
        val path = Paths.get("")
        for {
          fsItem <- fileOps.read(path)
          layout = fsItem match {
            case TextFile(_) =>
              fromPath(path)
            case Dir(_, fileNames) =>
              fromDir(fileNames.toSet)
            case FileNotFound =>
              none[BuildToolLayout]
          }
        } yield layout.getOrElse(Unknown)
      }
    }

}
