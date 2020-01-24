package ru.d10xa.jadd.code.inserts

import com.typesafe.scalalogging.LazyLogging
import ru.d10xa.jadd.show.SbtFormatShowPrinter
import ru.d10xa.jadd.view.ArtifactView
import cats.implicits._
import ru.d10xa.jadd.core.Artifact

class SbtFileInserts extends LazyLogging {

  import ArtifactView._

  def debugMatches(artifact: Artifact, matches: Seq[Match]): Unit = {
    def matchesCount = s"matches count: ${matches.size.show}"
    def matchesView =
      matches.map(m => s"${m.start.show} ${m.value.show}")
    logger.debug(
      s"""${artifact.groupId.show}:${artifact.artifactId} $matchesCount ($matchesView)""")
  }

  def appendAll(source: String, artifacts: Seq[Artifact]): String =
    artifacts.foldLeft(source) { case (s, artifact) => append(s, artifact) }

  /**
    * @return updated source
    */
  def append[T](buildFileSource: String, artifact: Artifact): String = {
    val matches: Seq[Match] =
      new SbtArtifactMatcher(buildFileSource).find(artifact)

    debugMatches(artifact, matches)

    val artifactMatches: Seq[(Artifact, Seq[Match])] =
      matches
        .map(m => artifact.copy(inSequence = m.inSequence))
        .map(_ -> matches)

    val maybeFirstMatch: Option[(Artifact, Seq[Match])] =
      artifactMatches
        .sortBy(_._2.minBy(_.start).start)
        .find(_._2.nonEmpty)

    def ins(a: Artifact = artifact): String = {
      val insertStrings = SbtFormatShowPrinter.single(a)
      appendLines(buildFileSource.split('\n'), insertStrings :: Nil)
        .mkString("\n") + "\n"
    }

    maybeFirstMatch match {
      case None =>
        ins()
      case Some((a, ms)) =>
        ms.minBy(_.start)
          .replace(buildFileSource, SbtFormatShowPrinter.single(a))
    }
  }

  def appendLines(
    buildFileLines: Array[String],
    dependencies: Seq[String]): Seq[String] =
    buildFileLines.toVector ++ dependencies
}
