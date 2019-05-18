package ru.d10xa.jadd

import better.files._
import cats.effect.Sync
import cats.implicits._

trait ProjectFileReader {
  def read[F[_]: Sync](relative: String): F[String]
  def exists[F[_]: Sync](relative: String): F[Boolean]
  def file[F[_]: Sync](relative: String): F[File]
}

class ProjectFileReaderMemory(m: Map[String, String])
    extends ProjectFileReader {

  import collection.mutable

  private val tmpFiles = mutable.Map[String, File]()

  def read[F[_]: Sync](relative: String): F[String] =
    Sync[F].delay(m(relative))

  def exists[F[_]: Sync](relative: String): F[Boolean] =
    Sync[F].pure(m.contains(relative))

  def file[F[_]: Sync](relative: String): F[File] =
    tmpFiles.get(relative) match {
      case Some(file) => Sync[F].pure(file)
      case None =>
        val x: F[String] = read[F](relative)
        x.map { content =>
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

class ProjectFileReaderImpl(root: File) extends ProjectFileReader {

  override def file[F[_]: Sync](relative: String): F[File] = Sync[F].delay {
    require(!relative.startsWith("."), "relative can not starts with '.'")
    require(!relative.startsWith("/"), "relative can not starts with '/'")
    File(root, relative)
  }

  override def read[F[_]: Sync](relative: String): F[String] =
    file(relative)
      .map(_.contentAsString)

  override def exists[F[_]: Sync](relative: String): F[Boolean] =
    file(relative)
      .map(_.exists())

}
