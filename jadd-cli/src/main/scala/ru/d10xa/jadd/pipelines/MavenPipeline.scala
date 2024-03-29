package ru.d10xa.jadd.pipelines

import java.nio.file.Path
import java.nio.file.Paths
import cats.data.Chain
import cats.syntax.all._
import cats.effect._
import ru.d10xa.jadd.code.Indent
import ru.d10xa.jadd.code.Indentation
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.MavenFileInserts
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.show.MavenFormatShowPrinter
import ru.d10xa.jadd.show.MavenShowCommand
import ru.d10xa.jadd.versions.ScalaVersions

class MavenPipeline[F[_]: Sync](
  override val ctx: Ctx,
  fileOps: FileOps[F]
) extends Pipeline[F] {

  val buildFile: Path = Paths.get("pom.xml")

  def buildFileSource: F[TextFile] =
    for {
      textFile <- Utils.textFileFromPath(fileOps, buildFile)
      source = textFile
    } yield source

  def fix(artifacts: List[Artifact]): List[Artifact] =
    artifacts.map(_.inlineScalaVersion)

  def buildFileToIndent(buildFileSource: String): Indent =
    Indentation.predictIndentation(buildFileSource.split('\n').toIndexedSeq)

  def makeStringsForInsert(
    artifacts: List[Artifact],
    indent: Indent
  ): List[String] =
    fix(artifacts)
      .map(MavenFormatShowPrinter.singleWithIndent(_, indent))

  def newContent(
    buildFileSource: String,
    stringsForInsert: List[String],
    indent: Indent
  ): String =
    MavenFileInserts
      .append(
        buildFileSource,
        stringsForInsert.map(_.split('\n').toSeq),
        indent
      )
      .mkString("\n") + "\n"

  def sourceToNewSource(source: String, artifacts: List[Artifact]): String = {
    val indent = buildFileToIndent(source)
    val stringsForInsert = makeStringsForInsert(artifacts, indent)
    newContent(source, stringsForInsert, indent)
  }

  def install(artifacts: List[Artifact])(implicit logger: Logger[F]): F[Unit] =
    for {
      source <- buildFileSource
      newSource = sourceToNewSource(source.content.value, artifacts)
      _ <- fileOps.write(buildFile, newSource)
    } yield ()

  override def show()(implicit logger: Logger[F]): F[Chain[Artifact]] =
    for {
      source <- buildFileSource
      x <- Sync[F].delay(new MavenShowCommand(source.content.value).show())
    } yield x

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
