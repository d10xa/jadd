package ru.d10xa.jadd.core

import better.files._
import cats.effect.Sync
import cats.implicits._

trait ProjectFileReader[F[_]] {
  def read(relative: String): F[String]
  def exists(relative: String): F[Boolean]
  def file(relative: String): F[File]
}

class ProjectFileReaderMemory[F[_]: Sync](m: Map[String, String])
    extends ProjectFileReader[F] {

  import collection.mutable

  private val tmpFiles = mutable.Map[String, File]()

  def read(relative: String): F[String] =
    Sync[F].delay(m(relative))

  def exists(relative: String): F[Boolean] =
    Sync[F].pure(m.contains(relative))

  def file(relative: String): F[File] =
    tmpFiles.get(relative) match {
      case Some(file) => Sync[F].pure(file)
      case None =>
        read(relative)
          .map { content =>
            val fileName = relative
              .replace("/", "_")
              .replace("\\", "_")
              .split("\\.")
            File
              .newTemporaryFile("tempfile", fileName.last)
              .deleteOnExit()
              .write(content)
          }
    }
}

class ProjectFileReaderImpl[F[_]: Sync](root: File)
    extends ProjectFileReader[F] {

  override def file(relative: String): F[File] = Sync[F].delay {
    File(root, relative)
  }

  override def read(relative: String): F[String] =
    file(relative)
      .map(_.contentAsString)

  override def exists(relative: String): F[Boolean] =
    file(relative)
      .map(_.exists())

}
