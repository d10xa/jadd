package ru.d10xa.jadd.regex

import ru.lanwen.verbalregex.VerbalExpression

import scala.util.matching.Regex

object RegexImplicits {
  implicit class VerbalExpressionImplicits(ve: VerbalExpression) {
    def pattern: Regex = ve.toString.r
    def groups3(text: String, g1: Int = 1, g2: Int = 2, g3: Int = 3): Seq[(String, String, String)] = {
      pattern.findAllIn(text).matchData.map { m =>
        (m.group(g1), m.group(g2), m.group(g3))
      }.toList
    }
    def groups2(text: String, g1: Int = 1, g2: Int = 2): Seq[(String, String)] = {
      pattern.findAllIn(text).matchData.map { m =>
        (m.group(g1), m.group(g2))
      }.toList
    }
  }
}
