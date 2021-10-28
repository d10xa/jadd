package ru.d10xa.jadd.code.scalameta

import monocle.Lens

import java.nio.file.Path
import scala.meta.Defn
import scala.meta.Lit
import scala.meta.Pat
import scala.meta.Term
import scala.meta.inputs.Position

object ScalaMetaPatternMatching {

  final case class PercentChars(n: Int)

  object UnapplyPercentChars {

    /** Calculate the number of percent characters, but only if there are no
      * other characters in the string
      */
    def unapply(s: String): Option[Int] =
      if (s.nonEmpty && s.forall(_ == '%')) {
        Some(s.length)
      } else {
        None
      }
  }

  /** Match leftTerm % rightTerm
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
    * @param groupId
    *   org.something
    * @param artifactId
    *   something-name
    * @param percents
    *   %%
    */
  final case class GroupIdPercentArtifactId(
    groupId: VariableValue,
    artifactId: VariableValue,
    percents: PercentChars
  )

  object UnapplyGroupIdPercentArtifactId {
    def unapply(t: Term.ApplyInfix): Option[GroupIdPercentArtifactId] =
      t match {
        case Term
              .ApplyInfix(
                VariableValue(groupId),
                Term.Name(UnapplyPercentChars(p)),
                Nil,
                List(VariableValue(artifactId))
              ) =>
          Some(GroupIdPercentArtifactId(groupId, artifactId, PercentChars(p)))
        case _ => None
      }
  }

  sealed trait SbtTree {

    private def initScopePath(path: Path, tree: SbtTree): SbtTree = tree match {
      case v: Value => v
      case m: Module =>
        m.copy(
          groupId = m.groupId.withPath(path),
          artifactId = m.artifactId.withPath(path),
          version = m.version.withPath(path)
        )
      case scope: Scope =>
        scope.copy(
          filePath = Some(path),
          items = scope.items.map(item => initScopePath(path, item))
        )
    }

    def withFilePath(path: Path): SbtTree = initScopePath(path, this)

  }

  final case class Value(
    path: Vector[String],
    value: String,
    pos: Position,
    filePath: Path
  ) extends SbtTree {
    def prependPath(scopeName: String): Value =
      this.copy(path = scopeName +: path)
  }

  /** ModuleID in terms of SBT
    *
    * "org.something" %% "something-name" % "0.0.1"
    */
  final case class Module(
    groupId: VariableValue,
    percentsCount: Int,
    artifactId: VariableValue,
    version: VariableValue,
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
      trees: Vector[SbtTree],
      path: Path
    ): Option[Scope] =
      trees match {
        case vec if vec.isEmpty => None
        case vec => Some(Scope(name, vec, Some(path)))
      }
  }

  object UnapplyModule {
    val termsLens: Lens[Module, List[Term]] =
      Lens[Module, List[Term]](_.terms)(terms => module => module.copy(terms = terms))

    def unapply(t: Term.ApplyInfix): Option[Module] =
      t match {
        case UnapplyApplyInfixPercent(
              UnapplyGroupIdPercentArtifactId(ga),
              1,
              VariableValue(moduleVersion)
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

  final case class ValueNoFilePath(
    path: Vector[String],
    value: String,
    pos: Position
  ) {
    def withFilePath(filePath: Path): Value =
      Value(path = path, value = value, pos = pos, filePath = filePath)
  }

  object UnapplyVal {
    def unapply(t: Defn.Val): Option[ValueNoFilePath] = t match {
      case Defn.Val(
            _,
            List(Pat.Var(Term.Name(k))),
            None,
            lit @ Lit.String(v)
          ) =>
        Some(ValueNoFilePath(Vector(k), v, lit.pos))
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
