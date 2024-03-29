package ru.d10xa.jadd.fs

import java.nio.file.Path
import java.nio.file.Paths
import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import org.http4s.blaze.client.BlazeClientBuilder
import ru.d10xa.jadd.fs.FsItem.Dir
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.fs.testkit.ItTestBase
import ru.d10xa.jadd.github.GithubFileOps
import ru.d10xa.jadd.github.GithubUrlParser.GithubUrlParts
import ru.d10xa.jadd.instances._

class GithubFileOpsTest extends ItTestBase {
  import github4s.Github

  val githubResourceIO: Resource[IO, Github[IO]] =
    BlazeClientBuilder[IO].resource
      .map(client => Github[IO](client, None))

  def read(p: Path): IO[FsItem] =
    GithubFileOps
      .make(githubResourceIO, GithubUrlParts("d10xa", "jadd", None, None))
      .flatMap(_.read(p))

  test("file") {
    val gitignore =
      read(Paths.get(".gitignore"))
        .unsafeRunSync()
    gitignore match {
      case TextFile(content, _) =>
        assert(content.value.contains("target/"))
      case _ => assert(false)
    }
  }

  test("dir") {
    val dir =
      read(Path.of("jadd-cli/src")).unsafeRunSync()
    dir match {
      case Dir(_, files) =>
        (files.map(_.getFileName.show) should contain)
          .allOf("main", "test")
      case _ => assert(false)
    }
  }

  test("empty") {
    val notFound: FsItem =
      read(Path.of("404")).unsafeRunSync()
    notFound shouldBe FsItem.FileNotFound
  }

}
