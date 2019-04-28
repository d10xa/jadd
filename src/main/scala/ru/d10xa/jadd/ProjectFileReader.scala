package ru.d10xa.jadd

import better.files._
import cats.effect.SyncIO

trait ProjectFileReader {
  def read(relative: String): SyncIO[String]
  def exists(relative: String): SyncIO[Boolean]
  def file(relative: String): SyncIO[File]
}

class ProjectFileReaderMemory(m: Map[String, String])
    extends ProjectFileReader {

  import collection.mutable

  private val tmpFiles = mutable.Map[String, File]()

  def read(relative: String): SyncIO[String] =
    SyncIO(m(relative))
  def exists(relative: String): SyncIO[Boolean] =
    SyncIO.pure(m.contains(relative))
  def file(relative: String): SyncIO[File] = tmpFiles.get(relative) match {
    case Some(file) => SyncIO.pure(file)
    case None =>
      read(relative).map { content =>
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

  override def file(relative: String): SyncIO[File] = SyncIO {
    require(!relative.startsWith("."), "relative can not starts with '.'")
    require(!relative.startsWith("/"), "relative can not starts with '/'")
    File(root, relative)
  }

  override def read(relative: String): SyncIO[String] =
    file(relative)
      .map(_.contentAsString)

  override def exists(relative: String): SyncIO[Boolean] =
    file(relative)
      .map(_.exists())

}
