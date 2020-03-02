package ru.d10xa.jadd.fs

import cats.effect.SyncIO
import cats.effect.concurrent.Ref
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineMV
import ru.d10xa.jadd.core.types
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.testkit.TestBase

class LiveCachedFileOpsTest extends TestBase {
  test("cache") {
    val io = for {
      readCounter <- Ref.of[SyncIO, Int](0)
      cache <- Ref.of[SyncIO, FileCache](FileCache.empty)
      fileOpsMock <- LiveCachedFileOps.make[SyncIO](
        new FileOps[SyncIO] {
          override def read(fileName: types.FileName): SyncIO[types.FsItem] =
            for {
              _ <- readCounter.update(_ + 1)
            } yield TextFile(FileContent(""))
          override def write(
            fileName: types.FileName,
            value: String): SyncIO[Unit] = SyncIO.unit
        },
        cache
      )
      fn1 = FileName(refineMV[NonEmpty]("a"))
      fn2 = FileName(refineMV[NonEmpty]("b"))
      fn3 = FileName(refineMV[NonEmpty]("c"))
      _ <- fileOpsMock.read(fn1)
      _ <- readCounter.get.map((c: Int) => assert(c == 1))
      _ <- fileOpsMock.read(fn1)
      _ <- readCounter.get.map((c: Int) => assert(c == 1))
      _ <- fileOpsMock.read(fn2)
      _ <- readCounter.get.map((c: Int) => assert(c == 2))
      _ <- fileOpsMock.write(fn3, "")
      _ <- fileOpsMock.read(fn3)
      _ <- readCounter.get.map((c: Int) => assert(c == 2))
    } yield ()
    io.unsafeRunSync()
  }
}
