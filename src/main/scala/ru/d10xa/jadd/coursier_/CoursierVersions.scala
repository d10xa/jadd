package ru.d10xa.jadd.coursier_

import cats.Parallel
import cats.effect.ContextShift
import cats.effect.Sync
import coursier.Repository
import coursier.Versions
import coursier.cache.CachePolicy
import coursier.cache.FileCache
import coursier.core
import coursier.core.Module

trait CoursierVersions[F[_]] {
  def versions(repositories: Seq[Repository], module: Module): F[core.Versions]
}

object CoursierVersions {
  def make[F[_]: Sync: Parallel: ContextShift]: F[CoursierVersions[F]] =
    Sync[F].delay(new CoursierVersions[F] {

      implicit val S: coursier.util.Sync[F] = coursier.interop.cats
        .coursierSyncFromCats(Sync[F], Parallel[F], ContextShift[F])

      lazy val cache: FileCache[F] = FileCache[F]()(S).noCredentials
        .withCachePolicies(Seq(CachePolicy.ForceDownload))

      override def versions(
        repositories: Seq[Repository],
        module: Module): F[core.Versions] = {
        println(s"CoursierVersions: $repositories, $module")
        Versions(cache)
          .withRepositories(repositories)
          .withModule(module)
          .versions()
      }

    })
}
