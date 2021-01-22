package ru.d10xa.jadd.code.inserts

import cats.Monad
import cats.syntax.all._
import ru.d10xa.jadd.show.SbtFormatShowPrinter
import ru.d10xa.jadd.view.ArtifactView
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.log.Logger

class SbtFileInserts[F[_]: Monad]()(implicit
  logger: Logger[F]
) {

  import ArtifactView._

  def debugMatches(artifact: Artifact, matches: Seq[Match]): F[Unit] = {
    def matchesCount = s"matches count: ${matches.size.show}"
    def matchesView =
      matches.map(m => s"${m.start.show} ${m.value.show}")
    logger.debug(
      s"""${artifact.groupId.show}:${artifact.artifactId} $matchesCount ($matchesView)"""
    )
  }

  def appendAll(source: String, artifacts: Seq[Artifact]): F[String] =
    artifacts.foldLeftM[F, String](source) { case (s, artifact) =>
      append(s, artifact)
    }

  /** @return updated source
    */
  def append(buildFileSource: String, artifact: Artifact): F[String] = {
    val matches: Seq[Match] =
      new SbtArtifactMatcher(buildFileSource).find(artifact)

    val artifactMatches: Seq[(Artifact, Seq[Match])] =
      matches
        .map(m => artifact.copy(inSequence = m.inSequence))
        .map(_ -> matches)

    val maybeFirstMatch: Option[(Artifact, Seq[Match])] =
      artifactMatches
        .sortBy(_._2.minBy(_.start).start)
        .find(_._2.nonEmpty)

    def ins: String = {
      val insertStrings = SbtFormatShowPrinter.single(artifact)
      appendLines(buildFileSource.split('\n'), insertStrings :: Nil)
        .mkString("\n") + "\n"
    }

    debugMatches(artifact, matches) *> (maybeFirstMatch match {
      case None =>
        ins.pure[F]
      case Some((a, ms)) =>
        ms.minBy(_.start)
          .replace(buildFileSource, SbtFormatShowPrinter.single(a))
          .pure[F]
    })
  }

  def appendLines(
    buildFileLines: Array[String],
    dependencies: Seq[String]
  ): Seq[String] =
    buildFileLines.toVector ++ dependencies
}
