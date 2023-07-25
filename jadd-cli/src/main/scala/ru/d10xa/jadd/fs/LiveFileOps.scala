package ru.d10xa.jadd.fs

import cats.effect.Sync
import cats.syntax.all._
import ru.d10xa.jadd.core.types.FileContent

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

class LiveFileOps[F[_]: Sync] private (path: Path) extends FileOps[F] {
  override def read(localPath: Path): F[FsItem] =
    for {
      resolvedPath <- path.resolve(localPath).pure[F]
      file <- Sync[F].delay(better.files.File(resolvedPath))
      isFile <- Sync[F].delay(file.isRegularFile)
      isDirectory <- Sync[F].delay(file.isDirectory)
      fsItem <-
        if (isFile) {
          Sync[F]
            .delay(
              FsItem
                .TextFile(FileContent(file.contentAsString), resolvedPath)
            )
            .widen[FsItem]
        } else if (isDirectory) {
          Sync[F]
            .delay {
              file.list
                .map(f => localPath.resolve(f.name))
                .toList
            }
            .map(list => FsItem.Dir(localPath, list))
            .widen[FsItem]
        } else { Sync[F].pure[FsItem](FsItem.FileNotFound) }
    } yield fsItem

  override def write(localPath: Path, value: String): F[Unit] =
    Sync[F].delay(
      better.files
        .File(path.resolve(localPath))
        .createFileIfNotExists(createParents = true)
        .write(value)
    )
}

object LiveFileOps {
  def make[F[_]: Sync](path: Path): F[FileOps[F]] =
    Sync[F]
      .delay {
        Files.exists(path, LinkOption.NOFOLLOW_LINKS)
      }
      .ensure(
        new IllegalArgumentException(
          s"file/directory does not exist [${path.toFile.getAbsolutePath}]"
        )
      )(exists => exists) *>
      Sync[F].delay(new LiveFileOps[F](path))
}
