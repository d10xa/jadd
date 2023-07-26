package ru.d10xa.jadd.fs

import java.nio.file.Path

// TODO @newtype
case class FileCache(value: Map[Path, FsItem])

object FileCache {
  val empty: FileCache = FileCache(Map.empty[Path, FsItem])
}
