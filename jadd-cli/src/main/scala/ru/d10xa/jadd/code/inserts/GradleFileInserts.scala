package ru.d10xa.jadd.code.inserts

import ru.d10xa.jadd.code.Indent
import ru.d10xa.jadd.code.Indentation
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.CodeBlock
import ru.d10xa.jadd.show.GradleFormatShowPrinter
import ru.d10xa.jadd.show.GradleLang.Groovy

class GradleFileInserts {

  import ru.d10xa.jadd.view.ArtifactView._

  def appendAll(source: String, artifacts: Seq[Artifact]): String =
    artifacts.foldLeft(source) { case (s, artifact) => append(s, artifact) }

  private def append(source: String, artifact: Artifact): String = {
    val sourceLines = source.split('\n').toIndexedSeq
    val indent: Indent = Indentation.predictIndentation(sourceLines)
    val blocks: Seq[CodeBlock] = CodeBlock.find(source, "dependencies {")

    val matches = new GradleArtifactMatcher(source).find(artifact)
    val maybeFirstMatch = matches.sortBy(_.start).headOption

    def insertOrUpdate(artifact: Artifact): String = {
      val str = new GradleFormatShowPrinter(Groovy) // TODO add kotlin
        .single(artifact.inlineScalaVersion)

      def insert(): String =
        if (blocks.isEmpty) {
          s"""$source
             |dependencies {
             |${indent.take(1)}$str
             |}
             |""".stripMargin
        } else {
          val b = blocks.head
          val (begin, end) = source.splitAt(b.innerEndIndex)
          begin + s"""${indent.take(1)}$str""" + "\n" + end
        }
      def update(m: Match): String =
        m.replace(source, str)

      maybeFirstMatch match {
        case Some(m: Match) => update(m)
        case None => insert()
      }
    }

    val matchedArtifact = maybeFirstMatch
      .map(m =>
        artifact
          .copy(
            configuration = Some(m.configuration),
            doubleQuotes = m.doubleQuotes
          )
      )
      .getOrElse(artifact)

    insertOrUpdate(matchedArtifact)
  }

}
