package ru.d10xa.jadd.pipelines

import java.nio.file.Path
import java.nio.file.Paths
import cats.syntax.all._
import cats.data.Chain
import cats.effect._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.code.inserts.SbtFileInserts
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.show.SbtShowCommand

class SbtPipeline[F[_]: Sync](
  override val ctx: Ctx,
  scalaVersionFinder: ScalaVersionFinder[F],
  showCommand: SbtShowCommand[F],
  fileOps: FileOps[F]
) extends Pipeline[F]
    with StrictLogging {

  val buildFile: Path = Paths.get("build.sbt")

  def buildFileSource: F[TextFile] =
    for {
      textFile <- Utils.textFileFromString(fileOps, buildFile)
      source = textFile
    } yield source

  def install(artifacts: List[Artifact]): F[Unit] =
    for {
      source <- buildFileSource
      newSource: String = new SbtFileInserts()
        .appendAll(source.content.value, artifacts)
      _ <- fileUpdate(newSource)
    } yield ()

  def fileUpdate(str: String): F[Unit] =
    fileOps.write(buildFile, str)

  override def show(): F[Chain[Artifact]] =
    for {
      artifacts <- showCommand.show()
    } yield artifacts

  override def findScalaVersion(): F[Option[ScalaVersion]] =
    scalaVersionFinder.findScalaVersion()

}
