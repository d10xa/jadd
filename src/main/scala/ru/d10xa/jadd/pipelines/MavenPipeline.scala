package ru.d10xa.jadd.pipelines

import better.files._
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Indentation
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.MavenFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.MavenFormatShowPrinter
import ru.d10xa.jadd.show.MavenShowCommand
import ru.d10xa.jadd.versions.ScalaVersions

class MavenPipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  lazy val buildFile = File(ctx.config.projectDir, "pom.xml")

  lazy val buildFileSource: String = buildFile.contentAsString

  override def applicable[F[_]: Sync](): F[Boolean] =
    Sync[F].delay(buildFile.exists())

  def fix(artifacts: List[Artifact]): List[Artifact] =
    artifacts.map(_.inlineScalaVersion)

  override def install(artifacts: List[Artifact]): Unit = {

    val indent = Indentation.predictIndentation(buildFileSource.split('\n'))

    val stringsForInsert = fix(artifacts)
      .map(MavenFormatShowPrinter.singleWithIndent(_, indent))

    def newContent =
      MavenFileInserts
        .append(
          buildFileSource,
          stringsForInsert.map(_.split('\n').toSeq),
          indent
        )
        .mkString("\n") + "\n"
    new SafeFileWriter().write(buildFile, newContent)
  }

  override def show[F[_]: Sync](): F[Seq[Artifact]] =
    Sync[F].delay(new MavenShowCommand(buildFileSource).show())

  override def findScalaVersion[F[_]: Sync](): F[Option[String]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
