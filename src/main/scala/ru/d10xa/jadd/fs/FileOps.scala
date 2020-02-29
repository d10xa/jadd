package ru.d10xa.jadd.fs

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import eu.timepit.refined.collection._
import eu.timepit.refined._
import eu.timepit.refined.types.string.NonEmptyString
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.core.types.FsItem.TextFile

trait FileOps[F[_]] {
  def read(fileName: FileName): F[FsItem]
  def write(fileName: FileName, value: String): F[Unit]
}

class LiveFileOps[F[_]: Sync] private (path: Path) extends FileOps[F] {
  override def read(fileName: FileName): F[FsItem] =
    for {
      file <- Sync[F].delay(better.files.File(path, fileName.value.value))
      isFile <- Sync[F].delay(file.isRegularFile)
      isDirectory <- Sync[F].delay(file.isDirectory)
      fsItem <- if (isFile) {
        Sync[F]
          .delay(
            FsItem
              .TextFile(FileContent(file.contentAsString))
          )
          .widen[FsItem]
      } else if (isDirectory) {
        Sync[F]
          .fromEither {
            val e: Either[Throwable, List[NonEmptyString]] =
              file.list
                .map(_.name)
                .toList
                .traverse(
                  refineV[NonEmpty](_).leftMap(new IllegalArgumentException(_)))
            e
          }
          .map(list => FsItem.Dir(list.map(s => FileName(s))))
          .widen[FsItem]
      } else { Sync[F].pure[FsItem](FsItem.FileNotFound) }
    } yield fsItem

  override def write(fileName: FileName, value: String): F[Unit] =
    Sync[F].delay(
      better.files
        .File(path.resolve(fileName.value.value))
        .createFileIfNotExists(createParents = true)
        .write(value))
}

class LiveCachedFileOps[F[_]: Sync] private (
  fileOps: FileOps[F],
  cacheRef: Ref[F, FileCache])
    extends FileOps[F] {
  override def read(fileName: FileName): F[FsItem] =
    for {
      cache <- cacheRef.get
      fsItem <- cache.value.get(fileName) match {
        case Some(v) => v.pure[F]
        case None =>
          fileOps.read(fileName).flatMap { fsItem =>
            cacheRef
              .set(FileCache(cache.value + (fileName -> fsItem)))
              .map(_ => fsItem)
          }
      }
    } yield fsItem

  override def write(fileName: FileName, value: String): F[Unit] = {
    val write = fileOps.write(fileName, value)
    val updateCache = cacheRef.update(cache =>
      FileCache(cache.value + (fileName -> TextFile(FileContent(value)))))
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

object LiveFileOps {
  def make[F[_]: Sync](path: Path): F[FileOps[F]] =
    Sync[F]
      .delay {
        Files.exists(path, LinkOption.NOFOLLOW_LINKS)
      }
      .ensure(new IllegalArgumentException(
        s"file/directory does not exist [${path.toFile.getAbsolutePath}]"))(
        exists => exists) *>
      Sync[F].delay(new LiveFileOps[F](path))
}
