package ru.d10xa.jadd.code.scalameta

import monocle.Lens
import monocle.macros.GenLens

import java.nio.file.Path
import scala.meta.Defn
import scala.meta.Lit
import scala.meta.Pat
import scala.meta.Term
import scala.meta.Tree
import scala.meta.inputs.Position

object ScalaMetaPatternMatching {

  final case class PercentChars(n: Int)

  object UnapplyPercentChars {

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

  /** Match
    * leftTerm % rightTerm
    */
  object UnapplyApplyInfixPercent {
    def unapply(t: Term.ApplyInfix): Option[(Term, Int, Term)] =
      Some(t).collect {
        case Term.ApplyInfix(
              l,
              Term.Name(UnapplyPercentChars(p)),
              Nil,
              List(r)
            ) =>
          (l, p, r)
      }
  }

  object UnapplySelect {

    import scala.meta._

    def unapply(t: scala.meta.Term.Select): Option[Vector[String]] = t match {
      case Term.Select(Term.Name(a), Term.Name(b)) => Some(Vector(a, b))
      case Term.Select(select @ Term.Select(_, _), Term.Name(r)) =>
        unapply(select).map(_ :+ r)
      case _ => None
    }
  }

  /** sbt.librarymanagement.DependencyBuilders.OrganizationArtifactName
    *
    * "org.something" %% "something-name"
    *
    * @param groupId org.something
    * @param artifactId something-name
    * @param percents %%
    */
  final case class GroupIdPercentArtifactId(
    groupId: SString,
    artifactId: SString,
    percents: PercentChars
  )

  object UnapplyGroupIdPercentArtifactId {
    def unapply(t: Term.ApplyInfix): Option[GroupIdPercentArtifactId] =
      t match {
        case Term
              .ApplyInfix(
                SString(groupId),
                Term.Name(UnapplyPercentChars(p)),
                Nil,
                List(SString(artifactId))
              ) =>
          Some(GroupIdPercentArtifactId(groupId, artifactId, PercentChars(p)))
        case _ => None
      }
  }

  sealed trait SString
  final case class LitString(value: String, pos: Position) extends SString
  final case class TermNameCompound(values: Vector[String]) extends SString

  object SString {
    def unapply(t: Tree): Option[SString] = t match {
      case lit @ Lit.String(value) => Some(LitString(value, lit.pos))
      case Term.Name(value) => Some(TermNameCompound(Vector(value)))
      case UnapplySelect(strings) => Some(TermNameCompound(strings))
      case _ => None
    }
  }

  sealed trait SbtTree {
    def withFilePath(path: Path): SbtTree = this match {
      case scope: Scope => scope.copy(filePath = Some(path))
      case x => x
    }
    def withFilePathOpt(optPath: Option[Path]): SbtTree = this match {
      case scope: Scope if scope.filePath.isEmpty && optPath.isDefined =>
        scope.copy(filePath = optPath)
      case x => x
    }
  }

  final case class Value(path: Vector[String], value: String, pos: Position)
      extends SbtTree {
    def prependPath(scopeName: String): Value =
      Value(scopeName +: path, value, pos)
  }

  /** ModuleID in terms of SBT
    *
    * "org.something" %% "something-name" % "0.0.1"
    */
  final case class Module(
    groupId: SString,
    percentsCount: Int,
    artifactId: SString,
    version: SString,
    terms: List[Term]
  ) extends SbtTree

  final case class Scope(
    name: Option[String],
    items: Vector[SbtTree],
    filePath: Option[Path]
  ) extends SbtTree

  object Scope {
    def makeNonEmpty(
      name: Option[String],
      trees: Vector[SbtTree]
    ): Option[Scope] =
      trees match {
        case vec if vec.isEmpty => None
        case vec => Some(Scope(name, vec, None))
      }
  }

  object UnapplyModule {
    val termsLens: Lens[Module, List[Term]] = GenLens[Module](_.terms)

    def unapply(t: Term.ApplyInfix): Option[Module] =
      t match {
        case UnapplyApplyInfixPercent(
              UnapplyGroupIdPercentArtifactId(ga),
              1,
              SString(moduleVersion)
            ) =>
          Some(
            Module(
              groupId = ga.groupId,
              percentsCount = ga.percents.n,
              artifactId = ga.artifactId,
              version = moduleVersion,
              terms = List.empty
            )
          )
        case UnapplyApplyInfixPercent(UnapplyModule(mId), 1, term) =>
          Some(termsLens.modify(l => term :: l)(mId))
        case _ => None
      }
  }

  object UnapplyVal {
    def unapply(t: Defn.Val): Option[Value] = t match {
      case Defn.Val(
            _,
            List(Pat.Var(Term.Name(k))),
            None,
            lit @ Lit.String(v)
          ) =>
        Some(Value(Vector(k), v, lit.pos))
      case _ => None
    }
  }

  object UnapplyCollectionApply {
    private val collectionsSet: Set[String] = Set("Seq", "List", "Vector")
    def unapply(a: Term.Apply): Option[List[Term]] = a match {
      case Term.Apply(Term.Name(collection), terms)
          if collectionsSet.contains(collection) =>
        Some(terms)
      case _ => None
    }
  }

}
