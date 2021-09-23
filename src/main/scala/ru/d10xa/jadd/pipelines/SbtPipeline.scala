package ru.d10xa.jadd.pipelines

import cats.data.Chain
import cats.effect._
import cats.syntax.all._
import coursier.core.Version
import ru.d10xa.jadd.code.inserts.SbtFileUpserts
import ru.d10xa.jadd.code.inserts.SbtFileUpserts.SbtUpsertQuery
import ru.d10xa.jadd.code.scalameta.ScalametaUtils
import ru.d10xa.jadd.code.scalameta.VariableLit
import ru.d10xa.jadd.code.scalameta.VariableLitP
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.show.SbtFormatShowPrinter
import ru.d10xa.jadd.show.SbtShowCommand

import java.nio.file.Path
import java.nio.file.Paths
import scala.meta.inputs.Position
import scala.util.chaining._

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  scalaVersionFinder: ScalaVersionFinder[F],
  showCommand: SbtShowCommand[F],
  fileOps: FileOps[F],
  sbtFileUpserts: SbtFileUpserts[F]
) extends Pipeline[F] {

  def install(artifacts: List[Artifact])(implicit logger: Logger[F]): F[Unit] =
    for {
      upsertQuery <- sbtFileUpserts.upsert(artifacts)
      _ <- invokeUpsert(upsertQuery).flatMap(writeChanges)
    } yield ()

  final case class SbtUpsertChanges(
    filesToUpdate: Vector[(Path, String)]
  ) {
    def mapBuildSbt(f: String => String): SbtUpsertChanges =
      filesToUpdate
        .map {
          case (p, content) if p.getFileName.toString.endsWith("build.sbt") =>
            (p, f(content))
          case tuple => tuple
        }
        .pipe(SbtUpsertChanges.apply)
  }

  def extractVersionPositions(
    vector: Vector[(VariableLitP, Artifact)]
  ): Vector[Either[Artifact, (VariableLitP, Version)]] =
    vector.map { case (varLitP: VariableLitP, artifact: Artifact) =>
      artifact.maybeVersion match {
        case Some(version) => Right(varLitP -> version)
        case None => Left(artifact)
      }
    }

  def createQuery(
    vector: Vector[(VariableLitP, Artifact)]
  ): SbtUpsertQuery =
    extractVersionPositions(vector).partitionEither(identity) match {
      case (toInsert, toUpdate) =>
        SbtUpsertQuery.make(toInsert, toUpdate)
    }

  def quote(s: String): String = "\"" + s + "\""

  def update(
    q: SbtUpsertQuery
  ): F[SbtUpsertChanges] = {
    val buildSbtPath = Paths.get(ctx.config.projectDir, "build.sbt")

    def readFile(
      path: Path,
      vector: Vector[(VariableLit, Version)]
    ): F[(TextFile, Vector[(Position, String)])] = for {
      a <- Utils.textFileFromPath(fileOps, path)
      b = vector.map { case (varLit: VariableLit, version: Version) =>
        (varLit.pos, quote(version.repr))
      }
    } yield (a, b)

    def evalReplaces(
      tf: TextFile,
      replaces: Vector[(Position, String)]
    ): String =
      ScalametaUtils.replacePositions(tf.content.value, replaces.toList)

    def readFiles(
      q: SbtUpsertQuery
    ): F[List[(TextFile, Vector[(Position, String)])]] =
      q.ensureBuildSbt(buildSbtPath)
        .toUpdate
        .toList
        .traverse((readFile _).tupled)

    readFiles(q)
      .map(list =>
        list.map { case (tf, vector) => (tf.path, evalReplaces(tf, vector)) }
      )
      .map(x => SbtUpsertChanges(filesToUpdate = x.toVector))
  }

  def appendLines(
    buildFileLines: Array[String],
    dependencies: Seq[String]
  ): Seq[String] =
    buildFileLines.toVector ++ dependencies

  def insArt(buildFileSource: String, artifact: Artifact): String = {
    val insertStrings = SbtFormatShowPrinter.single(artifact)
    appendLines(buildFileSource.split('\n'), insertStrings :: Nil)
      .mkString("\n") + "\n"
  }

  def enforceEndsWithLineBreak(s: String): String =
    if (s.endsWith("\n")) s else s + "\n"

  def insert(buildFileSource: String, q: SbtUpsertQuery): String =
    if (q.toInsert.isEmpty) {
      buildFileSource
    } else {
      buildFileSource
        .pipe(enforceEndsWithLineBreak)
        .pipe(text =>
          text + q.toInsert.map(SbtFormatShowPrinter.single).mkString("\n")
        )
    }

  def invokeUpsert(q: SbtUpsertQuery): F[SbtUpsertChanges] =
    update(q)
      .map(_.mapBuildSbt(content => insert(content, q)))

  def writeChanges(c: SbtUpsertChanges): F[Unit] = c.filesToUpdate.traverse_ {
    case (path, content) => fileOps.write(path, content)
  }

  override def show()(implicit logger: Logger[F]): F[Chain[Artifact]] =
    for {
      artifacts <- showCommand.show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    scalaVersionFinder.findScalaVersion()

}
