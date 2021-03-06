package ru.d10xa.jadd.fs

import java.nio.file.Paths

import cats.effect.SyncIO
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.testkit.TestBase

class FileOpsTest extends TestBase {
  test("check state") {

    tempPathResource[SyncIO]
      .use { tempDir =>
        for {
          ops <- LiveFileOps.make[SyncIO](tempDir)
          a <- ops.read(Paths.get("hello.txt"))
          _ = a should be(FsItem.FileNotFound)
          _ <- ops.write(Paths.get("hello.txt"), "world")
          b <- ops.read(Paths.get("hello.txt"))
          _ = b should be(FsItem.TextFile(FileContent("world")))
          _ <- ops.write(Paths.get("hello.txt"), "world2")
          c <- ops.read(Paths.get("hello.txt"))
          _ = c should be(FsItem.TextFile(FileContent("world2")))
        } yield ()
      }
      .unsafeRunSync()
  }
}
