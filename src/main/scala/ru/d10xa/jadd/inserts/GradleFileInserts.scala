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

  def append(source: String, artifact: Artifact): String = {
    val sourceLines = source.split('\n')
    val indent: Indent = Indentation.predictIndentation(sourceLines)
    val blocks: Seq[CodeBlock] = CodeBlock.find(source, "dependencies {")

    val matches: Seq[Match] = artifact.findMatchesInSource(source)
    val maybeFirstMatch = matches.sortBy(_.start).headOption

    val artifactAsString = artifact.showLines.mkString("\n")

    def insert(): String = {

      if(blocks.isEmpty) {
          s"""$source
            |dependencies {
            |${indent.take()}$artifactAsString
            |}
            |""".stripMargin
      } else {
        val b = blocks.head
        val (begin, end) = source.splitAt(b.innerEndIndex)
        begin + s"""${indent.take()}$artifactAsString""" + "\n" + end
      }
    }
    def update(m: Match): String = {
      m.replace(source, artifactAsString)
    }

    maybeFirstMatch match {
      case Some(m: Match) => update(m)
      case None => insert()
    }
  }

  def appendOld(buildFileSource: String, dependencies: Seq[String]): Seq[String] = {
    val fileLines = buildFileSource.split('\n')

    val index = fileLines indexWhere (_.startsWith("dependencies {"))
    val (a, b) = fileLines splitAt index + 1
    val indent =
      if (b.head.startsWith("}")) "    "
      else b.head.takeWhile(c => c == ' ' || c == '\t')
    a ++ dependencies.map(indent + _) ++ b
  }

}
