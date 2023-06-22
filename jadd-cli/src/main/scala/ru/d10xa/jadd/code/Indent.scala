package ru.d10xa.jadd.code

final case class Indent(style: Char, size: Int) {
  require(Indent.isIndent(style), "indent must be space or tab char")
  def take(count: Int): String = style.toString * (size * count)
}
object Indent {
  def space(size: Int): Indent = Indent(' ', size)
  def tab(size: Int): Indent = Indent('\t', size)
  def isIndent(style: Char): Boolean = style == ' ' || style == '\t'
  def fromCodeLine(line: String): Option[Indent] =
    line match {
      case a if a.trim.isEmpty => None // empty line may contain strange indents
      case a =>
        val indents = a.takeWhile(c => c == ' ' || c == '\t')
        if (indents.isEmpty) None
        else Some(Indent(indents.head, indents.length))
    }
}
