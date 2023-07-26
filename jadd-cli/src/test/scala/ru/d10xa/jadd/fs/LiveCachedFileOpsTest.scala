package ru.d10xa.jadd.fs

import java.nio.file.Path
import java.nio.file.Paths
import cats.effect.SyncIO
import cats.effect.kernel.Ref
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.testkit.TestBase

class LiveCachedFileOpsTest extends TestBase {
  test("cache") {
    val io = for {
      readCounter <- Ref.of[SyncIO, Int](0)
      cache <- Ref.of[SyncIO, FileCache](FileCache.empty)
      fileOpsMock <- LiveCachedFileOps.make[SyncIO](
        new FileOps[SyncIO] {
          override def read(path: Path): SyncIO[FsItem] =
            for {
              _ <- readCounter.update(_ + 1)
            } yield TextFile(FileContent(""), path)
          override def write(path: Path, value: String): SyncIO[Unit] =
            SyncIO.unit
        },
        cache
      )
      pA = Paths.get("a")
      pB = Paths.get("b")
      pC = Paths.get("c")
      _ <- fileOpsMock.read(pA)
      _ <- readCounter.get.map((c: Int) => assert(c == 1))
      _ <- fileOpsMock.read(pA)
      _ <- readCounter.get.map((c: Int) => assert(c == 1))
      _ <- fileOpsMock.read(pB)
      _ <- readCounter.get.map((c: Int) => assert(c == 2))
      _ <- fileOpsMock.write(pC, "")
      _ <- fileOpsMock.read(pC)
      _ <- readCounter.get.map((c: Int) => assert(c == 2))
    } yield ()
    io.unsafeRunSync()
  }
}
