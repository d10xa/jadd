package ru.d10xa.jadd.pipelines

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.SafeFileWriter
import ru.d10xa.jadd.inserts.GradleFileInserts
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.show.GradleShowCommand
import better.files._
import cats.effect.Sync

class GradlePipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  lazy val buildFile: File = File(ctx.config.projectDir, "build.gradle")

  def buildFileSource: String = buildFile.contentAsString

  override def applicable[F[_]: Sync](): F[Boolean] =
    Sync[F].delay(buildFile.exists())

  override def install(artifacts: List[Artifact]): Unit = {
    val newSource: String = new GradleFileInserts()
      .appendAll(buildFileSource, artifacts)

    new SafeFileWriter().write(buildFile, newSource)
  }

  override def show(): Seq[Artifact] =
    new GradleShowCommand(buildFileSource).show()

}
