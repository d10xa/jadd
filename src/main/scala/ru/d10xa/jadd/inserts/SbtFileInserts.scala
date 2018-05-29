package ru.d10xa.jadd.inserts

import com.typesafe.scalalogging.LazyLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.view.ArtifactView

class SbtFileInserts extends LazyLogging {

  import ArtifactView._
  import SbtPipeline._

  def debugMatches(artifact: Artifact, matches: Seq[Match]): Unit = {
    def matchesCount = s"matches count: ${matches.size}"
    def matchesView = matches.map(m => m.start + " " + m.value)
    logger.debug(s"""${artifact.groupId}:${artifact.artifactId} $matchesCount ($matchesView)""")
  }

  /**
   * @return updated source
   */
  def append[T](buildFileSource: String, artifact: Artifact): String = {
    val matches: Seq[Match] = artifact.find(buildFileSource)

    debugMatches(artifact, matches)

    val artifactMatches: Seq[(Artifact, Seq[Match])] =
      matches
        .map(m => artifact.copy(inSequence = m.inSequence))
        .map(_ -> matches)

    val foundWithMatches: Option[(Artifact, Seq[Match])] =
      artifactMatches
        .sortBy(_._2.minBy(_.start).start)
        .find(_._2.nonEmpty)

    def ins(a: Artifact = artifact): String = {
      val insertStrings = a.showLines
      appendLines(buildFileSource.split('\n'), insertStrings).mkString("\n") + "\n"
    }

    foundWithMatches match {
      case None =>
        ins()
      case Some((a, ms)) =>
        ms.minBy(_.start).replace(buildFileSource, a.showLines.mkString("\n"))
    }
  }

  def appendLines(buildFileLines: Array[String], dependencies: Seq[String]): Seq[String] =
    buildFileLines.toVector ++ dependencies
}
