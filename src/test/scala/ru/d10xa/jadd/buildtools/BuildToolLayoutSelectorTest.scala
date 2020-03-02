package ru.d10xa.jadd.buildtools

import cats.effect.Sync
import cats.effect.SyncIO
import org.scalatest.BeforeAndAfterAll
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.testkit.TestBase

class BuildToolLayoutSelectorTest extends TestBase with BeforeAndAfterAll {

  def createLayoutF[F[_]: Sync](
    files: List[(String, String)]): F[BuildToolLayout] =
    createLayoutSelectorWithFilesF[F](files)
      .use {
        case (path, selector) =>
          selector.select(
            Ctx(config =
              Config.empty.copy(projectDir = path.toFile.getAbsolutePath)))
      }

  val createLayout: List[(String, String)] => BuildToolLayout =
    (createLayoutF[SyncIO] _).andThen(_.unsafeRunSync())

  test("sbt") {
    createLayout(List("build.sbt" -> "")) shouldBe BuildToolLayout.Sbt
  }

  test("maven") {
    createLayout(List("pom.xml" -> "")) shouldBe BuildToolLayout.Maven
  }

  test("gradle") {
    createLayout(List("build.gradle" -> "")) shouldBe BuildToolLayout.Gradle
  }

  test("unknown") {
    createLayout(List(".gitignore" -> "")) shouldBe BuildToolLayout.Unknown
  }

}
