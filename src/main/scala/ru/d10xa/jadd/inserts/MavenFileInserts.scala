package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.Indent
import ru.d10xa.jadd.Indentation

object MavenFileInserts {

  def append(buildFileSource: String, dependencies: Seq[Seq[String]], indent: Indent): Seq[String] = {
    def isSuitableDependenciesTag(line: String): Boolean =
      line.trim.equals("<dependencies>") && Indentation.lineIndentation(line).exists(_.size == indent.size)

    val fileLines = buildFileSource.split('\n')

    val optLineIndex: Option[Int] = fileLines
      .zipWithIndex
      .collectFirst {
        case (line, index) if isSuitableDependenciesTag(line) => index
      }

    optLineIndex match {
      case Some(index) =>
        val d: Seq[String] = dependencies.flatMap(dependencyRows => dependencyRows.map(r => {
          s"${indent.take(2)}$r"
        }))
        MiddleInsert.insert(fileLines, d, index + 1)
      case None =>
        val d: Seq[String] = dependencies.flatten.map(d => s"${indent.take(1)}$d")
        val deps: Seq[String] =
          s"<dependencies>" +: d :+ s"</dependencies>"
        MiddleInsert.insert(fileLines, deps.map(d => s"${indent.take(1)}$d"), -1)
    }

  }

}
