package ru.d10xa.jadd.code.scalameta

import scala.meta.Term

/**
  * Match:
  * libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.18"
  */
object LibraryDependencies {
  object moduleIds {
    def unapplySeq(arg: Seq[Term]): Option[Seq[ModuleId]] = {
      val x = arg.collect {
        case ModuleIdMatch(mId) => mId
      }
      if (x.isEmpty) None else Some(x)
    }
  }

  def unapply(t: Term.ApplyInfix): Option[List[ModuleId]] = Some(t).collect {
    case Term.ApplyInfix(
        Term.Name("libraryDependencies"),
        Term.Name("+="),
        Nil,
        List(ModuleIdMatch(mId))) =>
      List(mId)
    case Term.ApplyInfix(
        Term.Name("libraryDependencies"),
        Term.Name("++="),
        Nil,
        List(
          Term.Apply(
            Term.Name("Seq" | "List" | "Vector"),
            moduleIds(mIds @ _*)
          ))) =>
      mIds.toList
  }
}
