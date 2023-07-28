package ru.d10xa.jadd.fs

import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.all._
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.fs.FsItem.TextFile

import java.nio.file.Path

class LiveCachedFileOps[F[_]: Sync] private (
  fileOps: FileOps[F],
  cacheRef: Ref[F, FileCache]
) extends FileOps[F] {
  override def read(path: Path): F[FsItem] =
    for {
      cache <- cacheRef.get
      fsItem <- cache.value.get(path) match {
        case Some(v) => v.pure[F]
        case None =>
          fileOps.read(path).flatMap { fsItem =>
            cacheRef
              .set(FileCache(cache.value + (path -> fsItem)))
              .map(_ => fsItem)
          }
      }
    } yield fsItem

  override def write(path: Path, value: String): F[Unit] = {
    val write = fileOps.write(path, value)
    val updateCache = cacheRef.update(cache =>
      FileCache(cache.value + (path -> TextFile(FileContent(value), path)))
    )
    write *> updateCache
  }
}

object LiveCachedFileOps {
  def make[F[_]: Sync](
    fileOps: FileOps[F],
    cacheRef: Ref[F, FileCache]
  ): F[FileOps[F]] =
    Sync[F].delay(new LiveCachedFileOps[F](fileOps, cacheRef))
}
