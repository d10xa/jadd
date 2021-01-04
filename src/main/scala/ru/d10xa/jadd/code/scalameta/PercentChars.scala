package ru.d10xa.jadd.code.scalameta

final case class PercentChars(n: Int)

object PercentCharsMatch {

  /** Calculate the number of percent characters,
    * but only if there are no other characters in the string
    */
  def unapply(s: String): Option[Int] =
    if (s.nonEmpty && s.forall(_ == '%')) {
      Some(s.length)
    } else {
      None
    }
}
