package ru.d10xa.jadd.inserts

import com.typesafe.scalalogging.LazyLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.view.ArtifactView

class SbtFileInserts extends LazyLogging {

  import ArtifactView._
  import SbtPipeline._

  def append[T](buildFileSource: String, artifacts: Seq[Artifact]): String = {

    val (updates, inserts) = artifacts
      .map(a => a -> a.find(buildFileSource))
      .flatMap { case (artifact, matches) =>
          matches.map(m => artifact.copy(inSequence = m.inSequence)).map(_ -> matches) match {
            case x if x.isEmpty => Seq(artifact -> matches)
            case x => x
          }
      }
      .distinct
      .span(_._2.nonEmpty)

    logger.debug(s"updates $updates")
    logger.debug(s"inserts $inserts")

    val insertStrings = inserts.map(_._1).flatMap(_.showLines)

    val appendResult: String = appendLines(buildFileSource.split('\n'), insertStrings).mkString("\n") + "\n"

    updates.foldLeft(appendResult) {
      case (source, (artifact, foundStrings)) =>
        foundStrings.minBy(_.start).replace(source, artifact.showLines.mkString("\n"))
    }
  }

  def appendLines(buildFileLines: Array[String], dependencies: Seq[String]): Seq[String] =
    buildFileLines.toVector ++ dependencies
}
