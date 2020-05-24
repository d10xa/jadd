package ru.d10xa.jadd.fs

import java.nio.file.Path

import cats.effect.IO
import ru.d10xa.jadd.fs.FsItem.Dir
import ru.d10xa.jadd.fs.FsItem.TextFile
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
        .read(Path.of(".gitignore"))
        .unsafeRunSync()
    gitignore match {
      case TextFile(content) =>
        assert(content.value.contains("target/"))
      case _ => assert(false)
    }
  }

  test("dir") {
    val dir =
      ops.read(Path.of("src")).unsafeRunSync()
    dir match {
      case Dir(_, files) =>
        (files.map(_.getFileName.toString) should contain)
          .allOf("main", "test")
      case _ => assert(false)
    }
  }

  test("empty") {
    val notFound: FsItem =
      ops.read(Path.of("404")).unsafeRunSync()
    notFound shouldBe FsItem.FileNotFound
  }

}
