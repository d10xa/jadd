package ru.d10xa.jadd.view

import ru.d10xa.jadd.view.ArtifactView.Match

import scala.language.implicitConversions

trait ArtifactView[T] {
  def showLines(value: T): Seq[String]
  def find(artifact: T, source: String): Seq[Match]
}

object ArtifactView {

  case class Match(start: Int, value: String, inSequence: Boolean = false) {
    require(value.nonEmpty, "match must be non empty")
    require(start > 0, "start of match must be positive")
    def replace(source: String, replacement: String): String = {
      source.substring(0, start) +
        replacement +
        source.substring(start + value.length)
    }
  }

  trait Ops[A] {
    def typeClassInstance: ArtifactView[A]
    def self: A
    def showLines: Seq[String] = typeClassInstance.showLines(self)
    def find(source: String): Seq[Match] = typeClassInstance.find(self, source)
  }
  implicit def artifactViewOps[T](target: T)(implicit t: ArtifactView[T]): Ops[T] = new Ops[T] {
    val self: T = target
    val typeClassInstance: ArtifactView[T] = t
  }
}
