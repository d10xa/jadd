package ru.d10xa.jadd.fs

import cats.effect.SyncIO
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.testkit.TestBase

class FileOpsTest extends TestBase {
  test("check state") {

    tempPathResource[SyncIO]
      .use { tempDir =>
        for {
          ops <- LiveFileOps.make[SyncIO](tempDir)
          a <- ops.read(FileName("hello.txt": NonEmptyString))
          _ = a should be(FsItem.FileNotFound)
          _ <- ops.write(FileName("hello.txt"), "world")
          b <- ops.read(FileName("hello.txt"))
          _ = b should be(FsItem.TextFile(FileContent("world")))
          _ <- ops.write(FileName("hello.txt"), "world2")
          c <- ops.read(FileName("hello.txt"))
          _ = c should be(FsItem.TextFile(FileContent("world2")))
        } yield ()
      }
      .unsafeRunSync()
  }
}
