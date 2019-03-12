package ru.d10xa.jadd.regex

import ru.lanwen.verbalregex.VerbalExpression

import scala.util.matching.Regex

object RegexImplicits {

  implicit class VerbalExpressionGroups(val verbalExpression: VerbalExpression)
      extends AnyVal {

    def pattern: Regex = verbalExpression.toString.r

    def groups[T](f: Regex.Match => T)(text: String): Seq[T] =
      pattern
        .findAllIn(text)
        .matchData
        .map(f)
        .toList

    def groups2(text: String): Seq[(String, String)] =
      groups(m => (m.group(1), m.group(2)))(text)

    def groups3(text: String): Seq[(String, String, String)] =
      groups(m => (m.group(1), m.group(2), m.group(3)))(text)

    def groups4(text: String): Seq[(String, String, String, String)] =
      groups(m => (m.group(1), m.group(2), m.group(3), m.group(4)))(text)

  }

}
