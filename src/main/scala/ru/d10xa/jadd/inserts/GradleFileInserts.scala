package ru.d10xa.jadd.inserts

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Indent
import ru.d10xa.jadd.Indentation
import ru.d10xa.jadd.experimental.CodeBlock

class GradleFileInserts extends StrictLogging {

  import ru.d10xa.jadd.view.ArtifactView._
  import ru.d10xa.jadd.implicits.gradle._

  def appendAll(source: String, artifacts: Seq[Artifact]): String =
    artifacts.foldLeft(source) { case (s, artifact) => append(s, artifact) }

  private def append(source: String, artifact: Artifact): String = {
    val sourceLines = source.split('\n')
    val indent: Indent = Indentation.predictIndentation(sourceLines)
    val blocks: Seq[CodeBlock] = CodeBlock.find(source, "dependencies {")

    val matches = new GradleArtifactMatcher(source).find(artifact)
    val maybeFirstMatch = matches.sortBy(_.start).headOption

    def insertOrUpdate(artifact: Artifact): String = {
      val artifactAsString = artifact.showLines.mkString("\n")

      def insert(): String =
        if (blocks.isEmpty) {
          s"""$source
             |dependencies {
             |${indent.take(1)}$artifactAsString
             |}
             |""".stripMargin
        } else {
          val b = blocks.head
          val (begin, end) = source.splitAt(b.innerEndIndex)
          begin + s"""${indent.take(1)}$artifactAsString""" + "\n" + end
        }
      def update(m: Match): String =
        m.replace(source, artifactAsString)

      maybeFirstMatch match {
        case Some(m: Match) => update(m)
        case None => insert()
      }
    }

    val matchedArtifact = maybeFirstMatch
      .map(
        m =>
          artifact
            .copy(
              configuration = Some(m.configuration),
              doubleQuotes = m.doubleQuotes))
      .getOrElse(artifact)

    insertOrUpdate(matchedArtifact)
  }

}
