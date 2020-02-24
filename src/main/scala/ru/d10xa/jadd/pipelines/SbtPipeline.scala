package ru.d10xa.jadd.pipelines

import cats.MonadError
import cats.data.Chain
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import monocle.Prism
import monocle.macros.GenPrism
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.types.FileCache
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.code.inserts.SbtFileInserts
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.SbtShowCommand

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder,
  scalaVersionFinder: ScalaVersionFinder[F],
  fileOps: FileOps[F])
    extends Pipeline[F]
    with StrictLogging {

  val buildFileName = "build.sbt"

  val textFilePrism: Prism[FsItem, TextFile] =
    GenPrism[FsItem, TextFile]

  val fileNameF: F[FileName] = FileName.make[F](buildFileName)

  val buildFileF: F[TextFile] = for {
    fileName <- fileNameF
    fsItem <- fileOps.read(fileName).runA(FileCache.empty)
    textFile <- MonadError[F, Throwable].fromOption(
      textFilePrism.getOption(fsItem),
      new IllegalStateException(s"File $buildFileName does not exist"))
  } yield textFile

  def buildFileSource: F[TextFile] =
    for {
      textFile <- buildFileF
      source = textFile
    } yield source

  override def applicable(): F[Boolean] =
    buildFileSource
      .map(_ => true)
      .recover { case _ => false }

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource: String = new SbtFileInserts()
        .appendAll(source.content.value, artifacts)
      _ <- fileUpdate(newSource)
    } yield ()

  def fileUpdate(str: String): F[Unit] =
    for {
      file <- fileNameF
      _ <- fileOps.write(file, str).runA(FileCache.empty)
    } yield ()

  override def show(): F[Chain[Artifact]] =
    for {
      artifacts <- new SbtShowCommand(
        fileOps,
        scalaVersionFinder
      ).show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    scalaVersionFinder.findScalaVersion()
}
