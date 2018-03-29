package ru.d10xa.jadd

final case class Indent(style: Char, size: Int) {
  require(style == ' ' || style == '\t')
  def take(count: Int = 1): String = style.toString * (size * count)
}
object Indent {
  def space(size: Int): Indent = Indent(' ', size)
  def tab(size: Int): Indent = Indent('\t', size)
  def isIndent(char: Char): Boolean = char == ' ' || char == '\t'
}
