package ru.d10xa.jadd.repl

import cats.effect.IO

import scala.collection.mutable

final class ArtifactAutocompleteCache(val deps: mutable.Set[String]) {

  private val visitedRemotely: mutable.HashSet[String] =
    mutable.HashSet[String]()

  def cache(completeModulePart: String, v: Vector[String]): IO[Unit] = IO {
    visitedRemotely.add(completeModulePart)
    v.foreach(deps.add)
  }

}
