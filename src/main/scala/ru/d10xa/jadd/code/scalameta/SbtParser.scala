package ru.d10xa.jadd.code.scalameta

import cats.Monad
import ru.d10xa.jadd.code.SbtFileUtils
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Module
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.log.Logger
import cats.syntax.all._
import ru.d10xa.jadd.code.scalameta.SbtParser.SbtParseResult

import scala.meta.Source
import scala.meta.dialects

trait SbtParser[F[_]] {

  /** It is possible for a module to be in multiple files. For example groupId
    * and artifactId defined at build.sbt but version defined at
    * Dependencies.sbt
    */
  def parse(fileOps: FileOps[F]): F[SbtParseResult]
}

object SbtParser {

  final case class SbtParseResult(
    files: Vector[TextFile],
    modules: Vector[Module]
  )

  def make[F[_]: Monad: Logger](
    sbtFileUtils: SbtFileUtils[F],
    sbtModuleParser: SbtModuleParser[F]
  ): F[SbtParser[F]] = new SbtParser[F] {
    override def parse(fileOps: FileOps[F]): F[SbtParseResult] =
      for {
        sbtFiles <- sbtFileUtils.sbtFiles
        sbtSources <- sbtFiles
          .traverse(fileOps.read)
          .map(_.collect { case t: TextFile => t })
        parsedSources = sbtSources
          .map { textFile =>
            dialects
              .Sbt1(textFile.content.value)
              .parse[Source]
              .toEither
              .map(source => textFile -> source)
          }
          .collect { case Right((textFile, source)) => (textFile, source) }
        trees = parsedSources.toVector.map { case (a, b) =>
          a.path -> b
        }
        modules <- sbtModuleParser.parse(trees)
      } yield SbtParseResult(sbtSources.toVector, modules)
  }.pure[F]
}
