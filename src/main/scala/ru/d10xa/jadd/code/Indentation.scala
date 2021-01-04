package ru.d10xa.jadd.code

object Indentation {

  def predictIndentation(
    fileLines: Seq[String],
    defaultIndent: Indent = Indent.space(4)
  ): Indent = {
    val indents: Seq[Option[Indent]] = fileLines.map(Indent.fromCodeLine)

    def countSizeMedian(seq: Seq[Int]): Int = {
      val sortedDiffs = seq
        .zip(seq.tail)
        .map { case (a, b) => math.abs(a - b) }
        .filter(_ > 0)
        .sorted
      def middle[T](seq: Seq[T]): T = seq(seq.size / 2)
      if (sortedDiffs.isEmpty) defaultIndent.size
      else middle(sortedDiffs)
    }

    def countStyleMedian(seq: Seq[Char]): Char = {
      val s = seq.sorted
      s(s.size / 2)
    }
    def indentsExists = indents.exists {
      case Some(Indent(_, size)) => size > 0
      case None => false
    }
    def styles = indents.collect { case Some(Indent(style, _)) => style }
    def sizes = indents.collect {
      case Some(Indent(_, size)) => size
      case None => 0
    }

    if (!indentsExists) {
      defaultIndent
    } else {
      Indent(countStyleMedian(styles), countSizeMedian(sizes))
    }
  }

}
