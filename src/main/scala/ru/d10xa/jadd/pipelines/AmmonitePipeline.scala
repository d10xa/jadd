package ru.d10xa.jadd.pipelines

import better.files._
import cats.effect._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.ProjectFileReader
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.show.AmmoniteFormatShowPrinter
import cats.implicits._
import ru.d10xa.jadd.versions.ScalaVersions

class AmmonitePipeline(
  override val ctx: Ctx,
  projectFileReader: ProjectFileReader
) extends Pipeline
    with StrictLogging {

  val buildFile: IO[File] =
    projectFileReader.file[IO](ctx.config.projectDir)

  override def applicable[F[_]: Sync](): F[Boolean] =
    for {
      file <- projectFileReader.file(ctx.config.projectDir)
      exists = file.exists
      isScalaScript = file.name.endsWith(".sc")
    } yield exists && isScalaScript

  lazy val buildFileSource: String =
    buildFile.map(_.contentAsString).unsafeRunSync()

  def install(artifacts: List[Artifact]): Unit = {
    val newDependencies = AmmoniteFormatShowPrinter.mkString(artifacts)
    val newSource: String =
      Seq(newDependencies, buildFileSource).mkString("\n")

    val fileUpdate = buildFile.map { f =>
      new SafeFileWriter().write(f, newSource)
    }

    fileUpdate.unsafeRunSync()
  }

  // TODO implement
  override def show[F[_]: Sync](): F[Seq[Artifact]] =
    Sync[F].delay(???)

  override def findScalaVersion[F[_]: Sync](): F[Option[String]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
