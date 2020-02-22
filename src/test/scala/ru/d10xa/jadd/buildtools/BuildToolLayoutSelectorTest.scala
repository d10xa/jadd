package ru.d10xa.jadd.buildtools

import java.nio.file.Path

import cats.effect.Resource
import cats.effect.Sync
import cats.effect.SyncIO
import org.scalatest.BeforeAndAfterAll
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.LiveFileOps
import ru.d10xa.jadd.testkit.TestBase
import cats.implicits._

class BuildToolLayoutSelectorTest extends TestBase with BeforeAndAfterAll {

  def fileOpsResource[F[_]: Sync]: Resource[F, (Path, FileOps[F])] =
    for {
      path <- tempPathResource[F]
      ops <- Resource.liftF(LiveFileOps.make[F](path))
    } yield (path, ops)

  def createLayoutF[F[_]: Sync](m: Map[String, String]): F[BuildToolLayout] =
    fileOpsResource.use {
      case (path, ops) =>
        val ctx = Ctx(
          config = Config.empty.copy(projectDir = path.toFile.getAbsolutePath))
        val createFiles = m.toList
          .traverse {
            case (fileName, content) =>
              Sync[F].delay(better.files.File(path, fileName).write(content))
          }
        val select = BuildToolLayoutSelector
          .make(ctx, fileOps = ops)
          .select()
        createFiles *> select
    }

  val createLayout: Map[String, String] => BuildToolLayout =
    (createLayoutF[SyncIO] _).andThen(_.unsafeRunSync())

  test("sbt") {
    createLayout(Map("build.sbt" -> "")) shouldBe BuildToolLayout.Sbt
  }

  test("maven") {
    createLayout(Map("pom.xml" -> "")) shouldBe BuildToolLayout.Maven
  }

  test("gradle") {
    createLayout(Map("build.gradle" -> "")) shouldBe BuildToolLayout.Gradle
  }

  test("unknown") {
    createLayout(Map(".gitignore" -> "")) shouldBe BuildToolLayout.Unknown
  }

}
