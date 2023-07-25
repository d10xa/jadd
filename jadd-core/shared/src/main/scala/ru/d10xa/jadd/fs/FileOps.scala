package ru.d10xa.jadd.fs

import java.nio.file.Path

trait FileOps[F[_]] {
  def read(path: Path): F[FsItem]
  def write(path: Path, value: String): F[Unit]
}
