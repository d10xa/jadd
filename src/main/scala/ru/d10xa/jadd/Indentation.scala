package ru.d10xa.jadd

object Indentation {

  def predictIndentation(fileLines: List[String]): (Char, Int) = {
    val indents = fileLines.map(lineIndentation)
    val nonEmptyIndents = indents.collect { case Some((c, i)) if i > 0 => (c, i) }
    if(nonEmptyIndents.isEmpty) {
      ' ' -> 4
    } else {
      val padSizes = nonEmptyIndents.map(_._2)
      val sortedIndentDiffs: Seq[Int] = (padSizes zip padSizes.tail).map {
        case (a, b) => math.abs(a - b)
      }.filter(_ > 0).sorted
      nonEmptyIndents.head._1 -> sortedIndentDiffs(sortedIndentDiffs.length / 2)
    }
  }

  def lineIndentation(line: String): Option[(Char, Int)] = {
    line match {
      case a if a.trim.isEmpty => None // empty line may contain strange indents
      case a =>
        val indents = a.takeWhile(c => c == ' ' || c == '\t')
        if (indents.isEmpty) None else Some(indents.head, indents.length)
    }
  }
}
