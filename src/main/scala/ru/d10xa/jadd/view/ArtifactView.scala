package ru.d10xa.jadd.view

import scala.language.implicitConversions

trait ArtifactView[T] {
  def showLines(value: T): Seq[String]
  def find(artifact: T, source: String): Option[String]
}

object ArtifactView {
  trait Ops[A] {
    def typeClassInstance: ArtifactView[A]
    def self: A
    def showLines: Seq[String] = typeClassInstance.showLines(self)
  }
  implicit def artifactViewOps[T](target: T)(implicit t: ArtifactView[T]): Ops[T] = new Ops[T] {
    val self: T = target
    val typeClassInstance: ArtifactView[T] = t
  }
}
