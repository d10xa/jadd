package ru.d10xa.jadd.testkit

import cats.syntax.all._

import cats.effect.Resource
import cats.effect.Sync
import ru.d10xa.jadd.code.SbtFileUtils
import ru.d10xa.jadd.code.inserts.SbtFileUpserts
import ru.d10xa.jadd.code.scalameta.SbtModuleParser
import ru.d10xa.jadd.code.scalameta.SbtParser
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.log.Logger

import java.nio.file.Path

trait TempSbtFileUpsertsTestSuite extends TempFileOpsTestSuite {

  def tempSbtFileUpsertsResource[F[_]: Sync: Logger: FilesF](
    files: (String, String)*
  ): Resource[F, (Path, FileOps[F], SbtFileUpserts[F])] = for {
    pathFileOps <- tempFileOpsResource[F](files: _*) // TODO better-monadic-for
    sbtFileUpserts <- Resource.eval(
      makeSbtFileUpserts[F](pathFileOps._2)
    )
  } yield (pathFileOps._1, pathFileOps._2, sbtFileUpserts)

  private def makeSbtFileUpserts[F[_]: Sync: FilesF](
    fileOps: FileOps[F]
  )(implicit logger: Logger[F]): F[SbtFileUpserts[F]] =
    for {
      sbtFileUtils <- SbtFileUtils.make[F](fileOps)
      sbtModuleParser <- SbtModuleParser.make[F]()
      sbtParser <- SbtParser.make[F](sbtFileUtils, sbtModuleParser)
      sbtFileUpserts <- SbtFileUpserts.make[F](sbtParser, fileOps)
    } yield sbtFileUpserts
}
