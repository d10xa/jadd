package ru.d10xa.jadd.core

import cats.ApplicativeError
import cats.effect.IO
import cats.effect.Resource
import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.troubles.ArtifactNotFoundByAlias
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import cats.implicits._
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.core.types.MonadThrowable
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.fs.FileOps

import scala.io.BufferedSource
import scala.io.Source

object Utils extends StrictLogging {
  def unshortAll(
    rawDependencies: List[String],
    artifactInfoFinder: ArtifactInfoFinder): List[Artifact] =
    rawDependencies
      .flatMap(
        raw =>
          artifactInfoFinder
            .artifactFromString[IO](raw)
            .unsafeRunSync() match { // TODO unsafeRunSync
            case Right(artifact) => artifact :: Nil
            case Left(_: ArtifactNotFoundByAlias) =>
              logger.info(s"$raw - artifact not found by shortcut")
              Nil
            case Left(trouble) =>
              logger.info(s"some error occurred $trouble")
              Nil
        })

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
      })

  def textFileFromString[F[_]: MonadThrowable](
    fileOps: FileOps[F],
    fileName: String): F[TextFile] =
    for {
      fsItem <- FsItem
        .fromFileNameString(fileName, fileOps)
      textFile <- ApplicativeError[F, Throwable].fromOption(
        FsItem.textFilePrism.getOption(fsItem),
        new IllegalStateException(s"File $fileName does not exist"))
    } yield textFile
}
