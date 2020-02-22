package ru.d10xa.jadd.fs

import java.nio.file.Files
import java.nio.file.Path

import cats.data.StateT._
import cats.effect.Resource
import cats.effect.SyncIO
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import org.apache.commons.io.FileUtils
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.testkit.TestBase

class FileOpsTest extends TestBase {
  test("check state") {

    val r: Resource[SyncIO, Path] =
      Resource.make[SyncIO, Path](
        SyncIO(Files.createTempDirectory(s"jadd_${getClass.getName}")))(path =>
        SyncIO(FileUtils.forceDelete(path.toFile)))

    r.use { tempDir =>
        val ops = LiveFileOps.make[SyncIO](tempDir).unsafeRunSync()
        val res = (for {
          a <- ops.read(FileName("hello.txt": NonEmptyString))
          _ = a should be(FsItem.FileNotFound)
          _ <- ops.write(FileName("hello.txt"), "world")
          b <- ops.read(FileName("hello.txt"))
          _ = b should be(FsItem.TextFile(FileContent("world")))
          _ <- ops.write(FileName("hello.txt"), "world2")
          c <- ops.read(FileName("hello.txt"))
          _ = c should be(FsItem.TextFile(FileContent("world2")))
          x <- inspect[SyncIO, FileCache, FileCache](identity)
          _ = x.value should be(Map(
            FileName("hello.txt") -> FsItem.TextFile(FileContent("world2"))))
        } yield x).runF
          .unsafeRunSync()(FileCache.empty)
        res
      }
      .unsafeRunSync()
  }
}
