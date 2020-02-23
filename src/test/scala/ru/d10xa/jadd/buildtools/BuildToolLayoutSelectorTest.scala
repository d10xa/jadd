package ru.d10xa.jadd.buildtools

import cats.effect.Sync
import cats.effect.SyncIO
import org.scalatest.BeforeAndAfterAll
import ru.d10xa.jadd.testkit.TestBase

class BuildToolLayoutSelectorTest extends TestBase with BeforeAndAfterAll {

  def createLayoutF[F[_]: Sync](
    files: List[(String, String)]): F[BuildToolLayout] =
    createLayoutSelectorWithFilesF[F](files)
      .use(_.select())

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
