package ru.d10xa.jadd.pipelines

import cats.data.Chain
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import eu.timepit.refined.collection.NonEmpty
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.show.AmmoniteFormatShowPrinter
import ru.d10xa.jadd.versions.ScalaVersions

class AmmonitePipeline[F[_]: Sync](
  override val ctx: Ctx,
  fileOps: FileOps[F]
) extends Pipeline[F]
    with StrictLogging {

  val buildFileName: String = ctx.config.projectDir

  val buildFileF: F[TextFile] =
    Utils.textFileFromString(fileOps, buildFileName)

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
      newDependencies <- Sync[F].delay(
        AmmoniteFormatShowPrinter.mkString(artifacts))
      source <- buildFileSource
      newSource = List(newDependencies, source.content.value).mkString("\n")
      fileName <- types.refineF[F, NonEmpty, String](buildFileName)
      _ <- fileOps.write(FileName(fileName), newSource)
    } yield ()

  // TODO implement
  override def show(): F[Chain[Artifact]] =
    Sync[F].delay(???)

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
