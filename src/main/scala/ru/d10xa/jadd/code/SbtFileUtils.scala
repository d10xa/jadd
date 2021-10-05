package ru.d10xa.jadd.code

import ru.d10xa.jadd.fs.FileOps
import cats.syntax.all._
import cats._
import ru.d10xa.jadd.fs.FsItem.Dir

import java.nio.file.Path
import java.nio.file.Paths
import scala.util.Try

trait SbtFileUtils[F[_]] {
  def buildFilePath: F[Path]
  def sbtFiles: F[List[Path]]
}

object SbtFileUtils {
  def make[F[_]: MonadThrow](fileOps: FileOps[F]): F[SbtFileUtils[F]] =
    new SbtFileUtils[F] {
      override def buildFilePath: F[Path] = MonadThrow[F].fromTry(Try {
        Paths.get("build.sbt")
      })

      private val otherSbtFilesF: F[List[Path]] =
        fileOps.read(Paths.get("project")).map {
          case Dir(_, names) => names
          case _ => List.empty[Path]
        }

      private def scalaFilePredicate(p: Path): Boolean = {
        import ru.d10xa.jadd.instances._
        val n = p.getFileName.show
        n.endsWith(".sbt") || n.endsWith(".scala")
      }

      override def sbtFiles: F[List[Path]] = for {
        buildSbt <- Paths.get("build.sbt").pure[F]
        other <- otherSbtFilesF
        otherFiltered <- other.filter(scalaFilePredicate).pure[F]
      } yield buildSbt :: otherFiltered

    }.pure[F]
}
