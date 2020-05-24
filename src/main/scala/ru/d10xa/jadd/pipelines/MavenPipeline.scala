package ru.d10xa.jadd.pipelines

import java.nio.file.Path

import cats.data.Chain
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.code.Indent
import ru.d10xa.jadd.code.Indentation
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.MavenFileInserts
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.MavenFormatShowPrinter
import ru.d10xa.jadd.show.MavenShowCommand
import ru.d10xa.jadd.versions.ScalaVersions

class MavenPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  fileOps: FileOps[F]
) extends Pipeline[F]
    with StrictLogging {

  val buildFile: Path = Path.of("pom.xml")

  def buildFileSource: F[TextFile] =
    for {
      textFile <- Utils.textFileFromString(fileOps, buildFile)
      source = textFile
    } yield source

  def fix(artifacts: List[Artifact]): List[Artifact] =
    artifacts.map(_.inlineScalaVersion)

  def buildFileToIndent(buildFileSource: String): Indent =
    Indentation.predictIndentation(buildFileSource.split('\n').toIndexedSeq)

  def makeStringsForInsert(
    artifacts: List[Artifact],
    indent: Indent): List[String] =
    fix(artifacts)
      .map(MavenFormatShowPrinter.singleWithIndent(_, indent))

  def newContent(
    buildFileSource: String,
    stringsForInsert: List[String],
    indent: Indent): String =
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

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource = sourceToNewSource(source.content.value, artifacts)
      _ <- fileOps.write(buildFile, newSource)
    } yield ()

  override def show(): F[Chain[Artifact]] =
    for {
      source <- buildFileSource
      x <- Sync[F].delay(new MavenShowCommand(source.content.value).show())
    } yield x

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
