package ru.d10xa.jadd.buildtools

import cats.effect.Sync
import ru.d10xa.jadd.core.Ctx
import cats.implicits._
import eu.timepit.refined.types.string.NonEmptyString
import ru.d10xa.jadd.buildtools.BuildToolLayout._
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.core.types.refineF

trait BuildToolLayoutSelector[F[_]] {
  def select(): F[BuildToolLayout]
}

object BuildToolLayoutSelector {
  def make[F[_]: Sync](
    ctx: Ctx,
    fileOps: FileOps[F]
  ): BuildToolLayoutSelector[F] =
    new BuildToolLayoutSelector[F] {

      private def fromFileName(n: FileName): Option[BuildToolLayout] =
        if (n.value.value.endsWith(".sc")) {
          Ammonite.some
        } else {
          none[BuildToolLayout]
        }
      private def fromDir(names: Set[FileName]): Option[BuildToolLayout] = {
        import eu.timepit.refined.auto._
        def sbt = names.contains(FileName("build.sbt": NonEmptyString))
        def maven = names.contains(FileName("pom.xml": NonEmptyString))
        def gradle = names.contains(FileName("build.gradle": NonEmptyString))
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

      override def select(): F[BuildToolLayout] = {
        import eu.timepit.refined.collection._
        for {
          name <- refineF[F, NonEmpty, String](ctx.config.projectDir)
          fileName = FileName(name)
          fsItem <- fileOps.read(FileName(name)).runA(FileCache.empty)
          layout = fsItem match {
            case FsItem.TextFile(_) =>
              fromFileName(fileName)
            case FsItem.Dir(fileNames) =>
              fromDir(fileNames.toSet)
            case _ =>
              none[BuildToolLayout]
          }
        } yield layout.getOrElse(Unknown)
      }
    }

}
