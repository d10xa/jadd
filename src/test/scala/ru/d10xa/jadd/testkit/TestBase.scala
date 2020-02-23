package ru.d10xa.jadd.testkit

import java.nio.file.Files
import java.nio.file.Path

import better.files.File
import cats.effect.Resource
import cats.effect.Sync
import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import ru.d10xa.jadd.buildtools.BuildToolLayoutSelector
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.LiveFileOps
import cats.implicits._

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
  def tempFileOpsResource[F[_]: Sync]: Resource[F, (Path, FileOps[F])] =
    for {
      path <- tempPathResource[F]
      ops <- Resource.liftF(LiveFileOps.make[F](path))
    } yield (path, ops)
  def createFileOpsWithFilesF[F[_]: Sync](
    files: List[(String, String)]): Resource[F, (Path, FileOps[F])] =
    tempFileOpsResource[F].flatMap {
      case t @ (path, ops) =>
        val createFiles: F[List[File]] = files
          .traverse {
            case (fileName, content) =>
              Sync[F].delay(
                better.files
                  .File(path.resolve(fileName))
                  .createFileIfNotExists(createParents = true)
                  .write(content))
          }
        Resource.liftF(createFiles.map(_ => t))
    }

  def createLayoutSelectorWithFilesF[F[_]: Sync](
    files: List[(String, String)]): Resource[F, BuildToolLayoutSelector[F]] =
    createFileOpsWithFilesF[F](files).map {
      case (path, ops) =>
        BuildToolLayoutSelector.make[F](
          Ctx(config =
            Config.empty.copy(projectDir = path.toFile.getAbsolutePath)),
          ops
        )
    }
}
