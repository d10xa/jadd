package ru.d10xa.jadd

sealed trait Scope

object Scope {
  case object Test extends Scope

  def scope(string: String): Scope = string match {
    case "test" => Test
    case _ => throw new IllegalArgumentException("wrong scope")
  }
}
