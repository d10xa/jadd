package ru.d10xa.jadd.testkit

import java.nio.file.Files
import java.nio.file.Path

import cats.effect.Resource
import cats.effect.Sync
import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.ScalaVersion

abstract class TestBase extends AnyFunSuiteLike with Matchers {
  implicit class ArtifactImplicits(private val artifact: Artifact) {
    def scala2_12: Artifact =
      artifact.copy(maybeScalaVersion = Some(ScalaVersion.fromString("2.12")))
    def scala2_11: Artifact =
      artifact.copy(maybeScalaVersion = Some(ScalaVersion.fromString("2.11")))
  }
  def art(s: String): Artifact = Artifact.fromString(s).toOption.get
  def tempPathResource[F[_]: Sync]: Resource[F, Path] =
    Resource.make[F, Path](
      Sync[F].delay(Files.createTempDirectory(s"jadd_${getClass.getName}")))(
      path => Sync[F].delay(FileUtils.forceDelete(path.toFile)))
}
