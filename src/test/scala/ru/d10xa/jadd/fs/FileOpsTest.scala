package ru.d10xa.jadd.fs

import java.nio.file.Files
import java.nio.file.Path

import cats.effect.Resource
import org.apache.commons.io.FileUtils

import cats.data.StateT._
import cats.effect.SyncIO
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.testkit.TestBase

class FileOpsTest extends TestBase {
  test("check state") {

    val r: Resource[SyncIO, Path] =
      Resource.make[SyncIO, Path](SyncIO(Files.createTempDirectory("tempdir")))(
        path => SyncIO(FileUtils.forceDelete(path.toFile)))

    r.use { tempDir =>
        val ops = LiveFileOps.make[SyncIO](tempDir).unsafeRunSync()
        val res = (for {
          a <- ops.read(FileName("hello.txt"))
          _ = assert(a.isEmpty)
          _ <- ops.write(FileName("hello.txt"), "world")
          b <- ops.read(FileName("hello.txt"))
          _ = assert(b.contains("world"))
          _ <- ops.write(FileName("hello.txt"), "world2")
          c <- ops.read(FileName("hello.txt"))
          _ = assert(c.contains("world2"))
          x <- inspect[SyncIO, FileCache, FileCache](identity)
          _ = assert(x.value == Map(FileName("hello.txt") -> "world2"))
        } yield x).runF
          .unsafeRunSync()(FileCache.empty)
        res
      }
      .unsafeRunSync()
  }
}
