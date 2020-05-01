package ru.d10xa.jadd.code.scalameta

import scala.meta.Term

/**
  * Match
  * leftTerm % rightTerm
  */
object ApplyInfixPercent {
  def unapply(t: Term.ApplyInfix): Option[(Term, Int, Term)] = Some(t).collect {
    case Term.ApplyInfix(l, Term.Name(PercentCharsMatch(p)), Nil, List(r)) =>
      (l, p, r)
  }
}
