package ru.d10xa.jadd.fs

import cats.effect.IO
import ru.d10xa.jadd.core.types
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.core.types.FsItem.Dir
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.fs.testkit.ItTestBase
import ru.d10xa.jadd.github.GithubFileOps

class GithubFileOpsTest extends ItTestBase {
  import github4s.Github
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val cs = IO.contextShift(global)
  val github = Github[IO]()
  val ops =
    new GithubFileOps[IO](github, "d10xa", "jadd", None)

  test("file") {
    val gitignore =
      ops
        .read(FileName(".gitignore"))
        .unsafeRunSync()
    gitignore match {
      case TextFile(content) =>
        assert(content.value.contains("target/"))
      case _ => assert(false)
    }
  }

  test("dir") {
    val dir =
      ops.read(FileName("src")).unsafeRunSync()
    dir match {
      case Dir(files) =>
        (files.map(_.value) should contain).allOf("main", "test")
      case _ => assert(false)
    }
  }

  test("empty") {
    val notFound: types.FsItem =
      ops.read(FileName("404")).unsafeRunSync()
    notFound shouldBe FsItem.FileNotFound
  }

}
