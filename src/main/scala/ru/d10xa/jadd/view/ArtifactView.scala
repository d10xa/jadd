package ru.d10xa.jadd.view

import ru.d10xa.jadd.core.CodeBlock

import scala.util.matching.Regex

// TODO Rename
object ArtifactView {

  sealed trait Match {
    def start: Int
    def value: String
    def inSequence: Boolean
    def replace(source: String, replacement: String): String =
      source.substring(0, start) +
        replacement +
        source.substring(start + value.length)
    def inBlock(blocks: Seq[CodeBlock]): Boolean =
      blocks.exists(b => b.innerStartIndex <= start && b.innerEndIndex >= start)
  }

  final case class MatchImpl(
    start: Int,
    value: String,
    inSequence: Boolean = false
  ) extends Match {
    require(value.nonEmpty, "match must be non empty")
    require(start > 0, "start of match must be positive")
  }

  final case class GradleMatchImpl(
    start: Int,
    value: String,
    configuration: String,
    doubleQuotes: Boolean
  ) extends Match {
    override def inSequence: Boolean = false
  }

  object Match {

    def inSequence[T <: Match](m: T, b: Boolean): Match = new Match {
      override def start: Int = m.start
      override def value: String = m.value
      override def inSequence: Boolean = b
    }

    def find(source: String, regexes: Seq[Regex]): Seq[Match] =
      for {
        regex <- regexes
        m <- regex.findAllMatchIn(source)
      } yield MatchImpl(start = m.start, value = m.group(0))
  }

}
