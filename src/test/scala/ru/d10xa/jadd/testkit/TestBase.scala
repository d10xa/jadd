package ru.d10xa.jadd.testkit

import java.nio.file.Path
import cats.effect.Resource
import cats.effect.Sync
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.ScalaVersion

abstract class TestBase
    extends AnyFunSuiteLike
    with Matchers
    with TempSbtFileUpsertsTestSuite {

  implicit class ArtifactImplicits(private val artifact: Artifact) {
    def scala2_12: Artifact =
      artifact.copy(maybeScalaVersion = Some(ScalaVersion.fromString("2.12")))
    def scala2_11: Artifact =
      artifact.copy(maybeScalaVersion = Some(ScalaVersion.fromString("2.11")))
  }

  implicit def filesF[F[_]: Sync]: FilesF[F] = new FilesF[F]

  def art(s: String): Artifact = Artifact.fromString(s).toOption.get

  def createLayoutSelectorWithFilesF[F[_]: Sync](
    files: List[(String, String)]
  ): Resource[F, (Path, BuildToolLayoutSelector[F])] =
    tempFileOpsResource[F](files: _*).map { case (path, ops) =>
      (path, BuildToolLayoutSelector.make[F](ops))
    }

}
