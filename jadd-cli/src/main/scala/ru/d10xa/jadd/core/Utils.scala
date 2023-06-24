package ru.d10xa.jadd.core

import cats.ApplicativeError
import cats.MonadThrow
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.all._
import ru.d10xa.jadd.core.troubles.ArtifactNotFoundByAlias
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.instances._
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import java.nio.file.Path
import scala.io.BufferedSource
import scala.io.Source

object Utils {

  def unshortAll[F[_]: Sync](
    rawDependencies: List[String],
    artifactInfoFinder: ArtifactInfoFinder[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[List[Artifact]] =
    rawDependencies.flatTraverse(s =>
      unshortOne(s, artifactInfoFinder, artifactShortcuts)
    )

  private def unshortOne[F[_]: Sync](
    raw: String,
    artifactInfoFinder: ArtifactInfoFinder[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[List[Artifact]] =
    artifactInfoFinder
      .artifactFromString(artifactShortcuts, raw)
      .flatMap {
        case Right(artifact) => (artifact :: Nil).pure[F]
        case Left(_: ArtifactNotFoundByAlias) =>
          logger.info(s"$raw - artifact not found by shortcut") *>
            List[Artifact]().pure[F]
        case Left(trouble) =>
          logger.info(s"some error occurred $trouble") *>
            List[Artifact]().pure[F]
      }

  def sourceFromSpringUri(string: String): BufferedSource =
    if (string.startsWith("classpath:")) Source.fromResource(string.drop(10))
    else Source.fromURL(string)

  def mkResource[F[_]: Sync](resource: String): Resource[F, BufferedSource] =
    Resource.make(if (resource.startsWith("classpath:")) {
      Sync[F].delay {
        val str = resource.drop(10)
        Source.fromResource(str)
      }
    } else if (resource.contains("://")) {
      Sync[F].delay(Source.fromURL(resource))
    } else {
      Sync[F].delay(Source.fromFile(resource))
    }) { r =>
      Sync[F].delay(r.close()).handleErrorWith(_ => Sync[F].unit)
    }

  def mkStringFromResourceF[F[_]: Sync](resource: String): F[String] =
    mkResource(resource)
      .use((r: BufferedSource) =>
        Sync[F].delay {
          r.mkString
        }
      )

  def textFileFromPath[F[_]: MonadThrow](
    fileOps: FileOps[F],
    path: Path
  ): F[TextFile] =
    for {
      fsItem <- fileOps.read(path)
      textFile <- ApplicativeError[F, Throwable].fromOption(
        FsItem.textFilePrism.getOption(fsItem),
        new IllegalStateException(
          s"File does not exist: ${path.toAbsolutePath.show}"
        )
      )
    } yield textFile
}
