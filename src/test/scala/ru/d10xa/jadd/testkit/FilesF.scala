package ru.d10xa.jadd.testkit

import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.all._

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class FilesF[F[_]: Sync]() {
  def deleteDirectoryRecursively(path: Path): F[Unit] =
    Sync[F].delay {
      Files.walkFileTree(
        path,
        new SimpleFileVisitor[Path] {
          override def postVisitDirectory(
            dir: Path,
            exc: IOException
          ): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }

          override def visitFile(
            file: Path,
            attrs: BasicFileAttributes
          ): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }
        }
      )
    }
  def createDirectories(path: Path): F[Unit] =
    Sync[F].delay(Files.createDirectories(path.getParent))

  def createTempDirectory(): F[Path] =
    Sync[F].delay(Files.createTempDirectory("java_nio_tmp_dir"))

  def write(
    path: Path,
    text: String,
    openOptions: Seq[OpenOption] = Seq.empty,
    charset: Charset = Charset.defaultCharset()
  ): F[Unit] =
    for {
      _ <- createDirectories(path)
      _ <- Sync[F].delay {
        Files.write(path, text.getBytes(charset), openOptions: _*)
      }
    } yield ()

  def isInside(parent: Path, child: Path): Boolean =
    child
      .normalize()
      .toAbsolutePath
      .startsWith(parent.normalize().toAbsolutePath)

  def fillDirectory(dir: Path, files: (String, String)*): F[Unit] =
    files.toList.traverse_ { case (localPath, content) =>
      val resolved = dir.resolve(localPath)
      if (!isInside(dir, resolved))
        Sync[F].raiseError[Unit](
          new IllegalArgumentException(
            s"child file is outside of parent directory (${dir.toString}, ${resolved.toString})"
          )
        )
      else
        write(resolved, content)
    }

  def tempDirectoryResource(files: (String, String)*): Resource[F, Path] = {
    def acquire: F[Path] =
      for {
        dir <- createTempDirectory()
        _ <- fillDirectory(dir, files: _*)
      } yield dir
    Resource.make[F, Path](acquire)(deleteDirectoryRecursively)
  }
}
