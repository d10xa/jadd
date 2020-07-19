package ru.d10xa.jadd.code.scalameta

import scala.meta.Defn
import scala.meta.Lit
import scala.meta.Pat
import scala.meta.Term

final case class StringVal(name: String, value: String)

object StringValMatch {

  def unapply(t: Defn): Option[StringVal] =
    t match {
      case Defn.Val(
          Nil,
          List(Pat.Var(Term.Name(name))),
          None,
          Lit.String(value)) =>
        Some(StringVal(name, value))
      case _ => None
    }
}
