package ru.d10xa.jadd.pipelines

import java.nio.file.Path
import java.nio.file.Paths

import cats.syntax.all._
import cats.data.Chain
import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.GradleFileInserts
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.show.GradleShowCommand
import ru.d10xa.jadd.versions.ScalaVersions

class GradlePipeline[F[_]: Sync](
  override val ctx: Ctx,
  fileOps: FileOps[F]
) extends Pipeline[F]
    with StrictLogging {

  val buildFile: Path = Paths.get("build.gradle")

  def buildFileSource: F[TextFile] =
    for {
      textFile <- Utils.textFileFromString(fileOps, buildFile)
      source = textFile
    } yield source

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource = new GradleFileInserts()
        .appendAll(source.content.value, artifacts)
      _ <- fileOps.write(buildFile, newSource)
    } yield ()

  override def show(): F[Chain[Artifact]] =
    for {
      source <- buildFileSource
      artifacts = new GradleShowCommand(source.content.value).show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)
}
