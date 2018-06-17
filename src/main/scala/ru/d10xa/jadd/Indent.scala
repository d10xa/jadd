package ru.d10xa.jadd

final case class Indent(style: Char, size: Int) {
  require(Indent.isIndent(style), "indent must be space or tab char")
  def take(count: Int = 1): String = style.toString * (size * count)
}
object Indent {
  def space(size: Int): Indent = Indent(' ', size)
  def tab(size: Int): Indent = Indent('\t', size)
  def isIndent(style: Char): Boolean = style == ' ' || style == '\t'
}
