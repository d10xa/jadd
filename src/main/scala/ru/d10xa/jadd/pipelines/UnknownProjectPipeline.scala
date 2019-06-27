package ru.d10xa.jadd.pipelines

import cats.Show
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.versions.ScalaVersions

class UnknownProjectPipeline(
  override val ctx: Ctx,
  artifactInfoFinder: ArtifactInfoFinder
) extends Pipeline
    with StrictLogging {

  /**
    * used only if project directory is unrecognized
    */
  override def applicable[F[_]: Sync](): F[Boolean] =
    Sync[F].pure(true)

  override def install(artifacts: List[Artifact]): Unit = {

    logger.info(
      s"build tool not recognized in directory ${ctx.config.projectDir}")

    implicit val artifactShow: Show[Artifact] =
      Show[Artifact] { a =>
        s"""groupId: ${a.groupId}
           |artifactId: ${a.artifactId}
           |version: ${a.maybeVersion.getOrElse("???")}""".stripMargin
      }

    artifacts
      .map(_.inlineScalaVersion)
      .map(_.show)
      .foreach(i => logger.info(i))
  }

  override def show[F[_]: Sync](): F[Seq[Artifact]] = {
    val log =
      Sync[F].delay(logger.info("Unknown project type. Nothing to show"))
    val emptySeq: F[Seq[Artifact]] = Sync[F].pure(Seq.empty)
    log *> emptySeq
  }

  override def findScalaVersion[F[_]: Sync](): F[Option[String]] =
    Sync[F].pure(ScalaVersions.defaultScalaVersion.some)

}
