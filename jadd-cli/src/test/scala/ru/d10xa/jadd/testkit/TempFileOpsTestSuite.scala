package ru.d10xa.jadd.testkit

import cats.effect.Resource
import cats.effect.Sync
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.LiveFileOps

import java.nio.file.Path

trait TempFileOpsTestSuite {
  def tempFileOpsResource[F[_]: Sync](
    files: (String, String)*
  ): Resource[F, (Path, FileOps[F])] = for {
    path <- new FilesF[F].tempDirectoryResource(files: _*)
    fileOps <- Resource.eval(LiveFileOps.make[F](path))
  } yield (path, fileOps)
}
