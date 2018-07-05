package ru.d10xa.jadd.pipelines

import cats.Show
import cats.data.EitherT
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles._
import ru.d10xa.jadd.versions.VersionTools

class UnknownProjectPipeline(val ctx: Ctx)(implicit artifactInfoFinder: ArtifactInfoFinder)
  extends Pipeline
  with StrictLogging {

  /**
   * used only if project directory is unrecognized
   */
  override def applicable: Boolean = true

  override def install(): Unit = {

    println(s"build tool not recognized in directory ${ctx.config.projectDir}")

    implicit val artifactShow: Show[Artifact] =
      Show[Artifact] { a =>
        s"""groupId: ${a.groupId}
           |artifactId: ${a.artifactId}
           |version: ${a.maybeVersion.getOrElse("???")}""".stripMargin
      }

    val (e, a) = EitherT(loadAllArtifacts(VersionTools).toList)
      .map(_.inlineScalaVersion)
      .map(_.show)
      .value
      .separate

    a.foreach(println)
    if (e.nonEmpty) println("ERRORS:")
    handleTroubles(e, println)
  }

  override def show(): Unit = {
    logger.info("Unknown project type. Nothing to show")
  }

}
