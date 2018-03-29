package ru.d10xa.jadd.inserts

object MiddleInsert {
  def insert(lines: Seq[String], linesToInsert: Seq[String], index: Int): Seq[String] = {
    val i = if(index < 0) lines.length + index else index
    val (a, b) = lines splitAt i
    a ++ linesToInsert ++ b
  }
}
