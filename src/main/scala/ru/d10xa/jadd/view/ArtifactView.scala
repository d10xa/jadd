package ru.d10xa.jadd.view

import ru.d10xa.jadd.experimental.CodeBlock

import scala.language.implicitConversions
import scala.util.matching.Regex

trait ArtifactView[T] {
  def showLines(value: T): Seq[String]
}

object ArtifactView {

  final case class Match(
    start: Int,
    value: String,
    inSequence: Boolean = false) {
    require(value.nonEmpty, "match must be non empty")
    require(start > 0, "start of match must be positive")
    def replace(source: String, replacement: String): String =
      source.substring(0, start) +
        replacement +
        source.substring(start + value.length)
    def inBlock(blocks: Seq[CodeBlock]): Boolean =
      blocks.exists(b => b.innerStartIndex <= start && b.innerEndIndex >= start)
  }

  object Match {
    def find(source: String, regexes: Seq[Regex]): Seq[Match] =
      for {
        regex <- regexes
        m <- regex.findAllMatchIn(source)
      } yield Match(start = m.start, value = m.group(0))
  }

  trait Ops[A] {
    def typeClassInstance: ArtifactView[A]
    def self: A
    def showLines: Seq[String] = typeClassInstance.showLines(self)
  }
  implicit def artifactViewOps[T](target: T)(
    implicit t: ArtifactView[T]): Ops[T] = new Ops[T] {
    val self: T = target
    val typeClassInstance: ArtifactView[T] = t
  }
}
