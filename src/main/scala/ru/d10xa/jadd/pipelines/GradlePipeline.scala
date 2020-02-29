package ru.d10xa.jadd.pipelines

import cats.data.Chain
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import eu.timepit.refined
import eu.timepit.refined.collection.NonEmpty
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.GradleFileInserts
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.GradleShowCommand
import ru.d10xa.jadd.versions.ScalaVersions

class GradlePipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  fileOps: FileOps[F]
) extends Pipeline[F]
    with StrictLogging {

  val buildFileName = "build.gradle"

  val fileNameF: F[FileName] = FileName.make[F](buildFileName)

  def buildFileSource: F[TextFile] =
    for {
      textFile <- Utils.textFileFromString(fileOps, buildFileName)
      source = textFile
    } yield source

  override def applicable(): F[Boolean] =
    buildFileSource
      .map(_ => true)
      .recover { case _ => false }

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource = new GradleFileInserts()
        .appendAll(source.content.value, artifacts)
      _ <- fileOps.write(
        FileName(refined.refineMV[NonEmpty]("build.gradle")),
        newSource)
    } yield ()

  override def show(): F[Chain[Artifact]] =
    for {
      source <- buildFileSource
      artifacts = new GradleShowCommand(source.content.value).show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)
}
