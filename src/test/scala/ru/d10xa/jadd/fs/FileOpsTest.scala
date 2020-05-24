package ru.d10xa.jadd.fs

import java.nio.file.Path

import cats.effect.SyncIO
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.testkit.TestBase

class FileOpsTest extends TestBase {
  test("check state") {

    tempPathResource[SyncIO]
      .use { tempDir =>
        for {
          ops <- LiveFileOps.make[SyncIO](tempDir)
          a <- ops.read(Path.of("hello.txt"))
          _ = a should be(FsItem.FileNotFound)
          _ <- ops.write(Path.of("hello.txt"), "world")
          b <- ops.read(Path.of("hello.txt"))
          _ = b should be(FsItem.TextFile(FileContent("world")))
          _ <- ops.write(Path.of("hello.txt"), "world2")
          c <- ops.read(Path.of("hello.txt"))
          _ = c should be(FsItem.TextFile(FileContent("world2")))
        } yield ()
      }
      .unsafeRunSync()
  }
}
