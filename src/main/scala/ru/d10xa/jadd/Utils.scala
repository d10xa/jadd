package ru.d10xa.jadd

import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.ArtifactNotFoundByAlias

import scala.io.BufferedSource
import scala.io.Source

object Utils {

  def unshortAll(rawDependencies: List[String], artifactInfoFinder: ArtifactInfoFinder): List[Artifact] =
    rawDependencies
      .flatMap(raw => artifactInfoFinder.artifactFromString(raw) match {
        case Right(artifact) => artifact :: Nil
        case Left(_: ArtifactNotFoundByAlias) =>
          println(s"$raw - artifact not found by shortcut")
          Nil
        case Left(trouble) =>
          println(s"some error occurred $trouble")
          Nil
      })

  def sourceFromSpringUri(string: String): BufferedSource =
    if (string.startsWith("classpath:")) Source.fromResource(string.drop(10))
    else Source.fromURL(string)

}
