package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.Indentation

object MavenFileInserts {

  def append(fileLines: List[String], dependencies: List[List[String]]): List[String] = {
    val withIndex = fileLines.zipWithIndex
    val (c, i) = Indentation.predictIndentation(fileLines)
    val dependenciesLineInfo: Option[(String, Int, String)] =
      withIndex
        .filter { case (line, index) => line.trim.equals("<dependencies>") }
        .map { case (line, index) => (line, index, line.takeWhile(c => c == ' ' || c == '\t')) }
        .sortBy(_._3.length)
        .headOption

    dependenciesLineInfo match {
      case None =>
        val d: List[String] = dependencies.flatMap(dependencyRows => dependencyRows.map(r => s"${c.toString * i}$r"))
        val deps = s"${c.toString * i}<dependencies>" +: d :+ s"${c.toString * i}</dependencies>"
        MiddleInsert.insert(fileLines, deps, -1)
      case Some((_, index, indentChars)) =>
        val d: List[String] = dependencies.flatMap(dependencyRows => dependencyRows.map(r => {
          val str: String = c.toString * (i + indentChars.length)
          s"$str$r"
        }))
        MiddleInsert.insert(fileLines, d, index + 1)
    }

  }

}
