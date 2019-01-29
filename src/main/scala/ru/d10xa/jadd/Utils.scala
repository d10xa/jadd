package ru.d10xa.jadd

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.ArtifactNotFoundByAlias

import scala.io.BufferedSource
import scala.io.Source

trait Utils {
  def unshortAll(
    rawDependencies: List[String],
    artifactInfoFinder: ArtifactInfoFinder): List[Artifact]
  def sourceFromSpringUri(string: String): BufferedSource
  def mkStringFromResource(resource: String): String
}

object Utils extends Utils with StrictLogging {
  override def unshortAll(
    rawDependencies: List[String],
    artifactInfoFinder: ArtifactInfoFinder): List[Artifact] =
    rawDependencies
      .flatMap(raw =>
        artifactInfoFinder.artifactFromString(raw) match {
          case Right(artifact) => artifact :: Nil
          case Left(_: ArtifactNotFoundByAlias) =>
            logger.info(s"$raw - artifact not found by shortcut")
            Nil
          case Left(trouble) =>
            logger.info(s"some error occurred $trouble")
            Nil
      })

  override def sourceFromSpringUri(string: String): BufferedSource =
    if (string.startsWith("classpath:")) Source.fromResource(string.drop(10))
    else Source.fromURL(string)

  override def mkStringFromResource(resource: String): String = {
    val source = if (resource.startsWith("classpath:")) {
      val str = resource.drop(10)
      Source.fromResource(str)
    } else if (resource.contains("://")) {
      Source.fromURL(resource)
    } else {
      Source.fromFile(resource)
    }
    val s = source.mkString
    source.close()
    s
  }
}
