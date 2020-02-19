package ru.d10xa.jadd.fs

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

import cats.data._
import cats.effect.Sync
import cats.implicits._
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FileCache

trait FileOps[F[_]] {
  def read(fileName: FileName): StateT[F, FileCache, Option[String]]
  def write(fileName: FileName, value: String): StateT[F, FileCache, Unit]
}

class LiveFileOps[F[_]: Sync] private (path: Path) extends FileOps[F] {
  override def read(fileName: FileName): StateT[F, FileCache, Option[String]] =
    StateT { cache =>
      cache.value.get(fileName) match {
        case Some(content) =>
          Sync[F].pure((cache, content.some))
        case None =>
          for {
            file <- Sync[F].delay(better.files.File(path, fileName.value))
            isFile <- Sync[F].delay(file.isRegularFile)
            contentOpt <- if (isFile) {
              Sync[F].delay(file.contentAsString.some)
            } else { Sync[F].pure(none[String]) }
            newCache = contentOpt.fold(cache)(content =>
              FileCache(cache.value + (fileName -> content)))
          } yield newCache -> contentOpt
      }
    }

  override def write(
    fileName: FileName,
    value: String): StateT[F, FileCache, Unit] = StateT { cache =>
    Sync[F].delay(better.files.File(path, fileName.value).write(value)) *>
      Sync[F].pure(FileCache(cache.value + (fileName -> value)) -> ())
  }
}

object LiveFileOps {
  def make[F[_]: Sync](path: Path): F[FileOps[F]] =
    Sync[F]
      .delay {
        Files.exists(path, LinkOption.NOFOLLOW_LINKS)
      }
      .ensure(new IllegalArgumentException(
        f"file/directory does not exist [${path.toAbsolutePath.toString}]"))(
        exists => exists) *>
      Sync[F].delay(new LiveFileOps[F](path))
}
