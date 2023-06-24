package ru.d10xa.jadd.pipelines

import java.nio.file.Path
import java.nio.file.Paths
import cats.data.Chain
import cats.syntax.all._
import cats.effect._
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.show.AmmoniteFormatShowPrinter
import ru.d10xa.jadd.versions.ScalaVersions

class AmmonitePipeline[F[_]: Sync](
  override val ctx: Ctx,
  fileOps: FileOps[F]
) extends Pipeline[F] {

  val buildFile: Path = Paths.get(ctx.projectPath)

  val buildFileF: F[TextFile] =
    Utils.textFileFromPath(fileOps, buildFile)

  def buildFileSource: F[TextFile] =
    for {
      textFile <- buildFileF
      source = textFile
    } yield source

  def install(artifacts: List[Artifact])(implicit logger: Logger[F]): F[Unit] =
    for {
      newDependencies <- Sync[F].delay(
        AmmoniteFormatShowPrinter.mkString(artifacts)
      )
      source <- buildFileSource
      newSource = List(newDependencies, source.content.value).mkString("\n")
      _ <- fileOps.write(buildFile, newSource)
    } yield ()

  // TODO implement
  override def show()(implicit logger: Logger[F]): F[Chain[Artifact]] =
    Sync[F].delay(???)

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
