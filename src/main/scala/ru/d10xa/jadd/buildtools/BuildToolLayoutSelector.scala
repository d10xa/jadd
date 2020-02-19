package ru.d10xa.jadd.buildtools

import cats.effect.Sync
import ru.d10xa.jadd.core.Ctx
import cats.implicits._
import better.files._
import cats.Applicative
import ru.d10xa.jadd.buildtools.BuildToolLayout._
import ru.d10xa.jadd.core.ProjectFileReader

trait BuildToolLayoutSelector[F[_]] {
  def select(): F[BuildToolLayout]
}

object BuildToolLayoutSelector {
  def make[F[_]: Sync](
    ctx: Ctx,
    projectFileReader: ProjectFileReader[F]): BuildToolLayoutSelector[F] =
    new BuildToolLayoutSelector[F] {
      private def fromExistentFile(f: File): F[Option[BuildToolLayout]] =
        Sync[F]
          .delay(f)
          .map(file =>
            if (file.name.endsWith(".sc")) {
              Ammonite.some
            } else {
              none[BuildToolLayout]
          })

      private def fromExistentDirectory(f: File): F[Option[BuildToolLayout]] =
        Sync[F]
          .delay(f)
          .map(dir =>
            if (File(dir, "pom.xml").isRegularFile) {
              Maven.some
            } else if (File(dir, "build.sbt").isRegularFile) {
              Sbt.some
            } else if (File(dir, "build.gradle").isRegularFile) {
              Gradle.some
            } else {
              none[BuildToolLayout]
          })

      override def select(): F[BuildToolLayout] =
        for {
          file <- projectFileReader.file(ctx.config.projectDir)
          isFile = file.isRegularFile
          isDirectory = file.isDirectory
          optionResult <- if (isFile) fromExistentFile(file)
          else if (isDirectory) fromExistentDirectory(file)
          else Applicative[F].pure(none[BuildToolLayout])
        } yield optionResult.getOrElse(Unknown)
    }

}
