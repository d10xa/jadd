package ru.d10xa.jadd.code.scalameta

import cats.Applicative
import coursier.core.Version
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion

import scala.meta.Source
import scala.meta.Term
import cats.syntax.all._
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyCollectionApply
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.LitString
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Module
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyModule
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Scope
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.TermNameCompound
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.SbtTree
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyVal
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Value

import scala.annotation.tailrec
import scala.meta.Defn
import scala.meta.Pat
import scala.meta.Template

trait SbtArtifactsParser[F[_]] {
  def parse(
    sources: Vector[Source]
  ): F[Vector[Module]]
  def parseArtifacts(
    scalaVersion: ScalaVersion,
    sources: Vector[Source]
  ): F[Vector[Artifact]]
}

object SbtArtifactsParser {

  def make[F[_]: Applicative](): F[SbtArtifactsParser[F]] =
    Applicative[F].pure {
      new SbtArtifactsParser[F] {
        override def parse(sources: Vector[Source]): F[Vector[Module]] =
          SbtArtifactsParser.parse(sources).pure[F]
        override def parseArtifacts(
          scalaVersion: ScalaVersion,
          sources: Vector[Source]
        ): F[Vector[Artifact]] =
          SbtArtifactsParser
            .parse(sources)
            .map(r => moduleToArtifact(scalaVersion, r))
            .pure[F]
      }
    }

  def substituteVersion(
    valuesMap: ValuesMap,
    module: Module
  ): Option[Module] =
    module.version match {
      case TermNameCompound(termNames) =>
        valuesMap.get(termNames) match {
          case Some(value) =>
            Some(module.copy(version = LitString(value)))
          case None => None
        }
      case _ => None
    }

  def createValuesMap(values: Vector[Value]): ValuesMap =
    values.map { case Value(path, value) => (path, value) }.toMap

  def separateValues(trees: Vector[SbtTree]): (Vector[Value], Vector[SbtTree]) =
    trees.foldRight((Vector.empty[Value], Vector.empty[SbtTree])) {
      case (v @ Value(_, _), (values, trees)) => (v +: values, trees)
      case (t: SbtTree, (values, trees)) => (values, t +: trees)
    }

  def substituteVersionTree(
    valuesMap: ValuesMap,
    tree: SbtTree
  ): SbtTree =
    tree match {
      case scope @ Scope(_, items) =>
        val (values, others) = separateValues(items)
        val newValuesMap = valuesMap ++ createValuesMap(values)
        val newItems = others.map {
          case m @ Module(_, _, _, TermNameCompound(values), _) =>
            newValuesMap.get(values) match {
              case Some(value) => m.copy(version = LitString(value))
              case None => m
            }
          case s @ Scope(_, items) =>
            s.copy(items =
              items.map(i => substituteVersionTree(newValuesMap, i))
            )
          case x => x
        }
        scope.copy(items = newItems)
      case m @ Module(_, _, _, TermNameCompound(_), _) =>
        substituteVersion(valuesMap, m).getOrElse(m)
      case _ => tree
    }

  def extractValues(
    items: Vector[SbtTree]
  ): WithCounter[Vector[SbtTree]] =
    items
      .map {
        case Scope(Some(innerName), innerItems) =>
          val (values, others) = innerItems.partition(_.isInstanceOf[Value])
          val extractedValues =
            values.collect { case v: Value => v.prependPath(innerName) }
          val scopeWithoutValues = Scope(Some(innerName), others)
          (extractedValues.size, (extractedValues, scopeWithoutValues))
        case x => (0, (Vector.empty[Value], x))
      }
      .sumCounter
      .map(appendTree)

  def extractModules(items: Vector[SbtTree]): WithCounter[Vector[SbtTree]] = {
    val res = items.map {
      case Scope(Some(innerName), innerItems) =>
        val (modules, others) = innerItems.partition(_.isInstanceOf[Module])
        (
          modules.foldLeft(0, Vector.empty[Module])((acc, module) =>
            module match {
              case m: Module =>
                (acc._1 + 1, acc._2 :+ m)
            }
          ),
          Scope(Some(innerName), others)
        )
      case x => ((0, Vector.empty[Module]), x)
    }
    res
      .foldLeft(0, Vector.empty[(Vector[Module], SbtTree)]) { (acc, cur) =>
        (acc._1 + cur._1._1, acc._2 :+ (cur._1._2, cur._2))
      }
      .map(appendTree)
  }

  def substituteNearModuleWithLocalValue(
    items: Vector[SbtTree]
  ): WithCounter[Vector[SbtTree]] = {
    val localValues = items
      .collect { case v @ Value(_, _) => v }
      .foldLeft(Map.empty[Vector[String], String])((acc, cur) =>
        acc + ((cur.path, cur.value))
      )
    items.map {
      case m @ Module(_, _, _, TermNameCompound(values), _) =>
        localValues.get(values) match {
          case Some(value) =>
            1 -> m.copy(version = LitString(value))
          case None => (0, m)
        }
      case x => (0, x)
    }.sumCounter
  }

  private def appendTree(
    xs: Vector[(Vector[SbtTree], SbtTree)]
  ): Vector[SbtTree] =
    xs.flatMap { case (xs2, x) => xs2 :+ x }

  def innerEval(items: Vector[SbtTree]): WithCounter[Vector[SbtTree]] =
    substituteNearModuleWithLocalValue(items)
      .flatMap(extractValues)
      .flatMap(extractModules)
      .flatMap(reduceTrees)

  @tailrec
  def loopReduce(node: SbtTree): Option[SbtTree] =
    reduceTree(node) match {
      case (i, Some(tree)) if i > 0 => loopReduce(tree)
      case (_, treeOpt) => treeOpt
      case _ => None
    }

  def reduceTrees(nodes: Vector[SbtTree]): WithCounter[Vector[SbtTree]] =
    nodes.map(reduceTree).sumCounter.map(_.flatten)

  def reduceTree(node: SbtTree): WithCounter[Option[SbtTree]] = node match {
    case v: Value => 0 -> Some(v)
    case m: Module => 0 -> Some(m)
    case Scope(_, items) if items.isEmpty => 1 -> None
    case Scope(None, Vector(item)) => 1 -> Some(item)
    case scope @ Scope(_, items) =>
      innerEval(items) match {
        case (i, value) => i -> Some(scope.copy(items = value))
      }
  }

  def evalTrees(trees: List[scala.meta.Tree]): Option[SbtTree] =
    Scope.makeNonEmpty(name = none, trees = trees.flatMap(eval).toVector)

  def evalTreesNamed(
    name: String,
    trees: List[scala.meta.Tree]
  ): Option[SbtTree] =
    Scope.makeNonEmpty(name.some, trees.flatMap(eval).toVector)

  def eval(tree: scala.meta.Tree): Option[SbtTree] =
    tree match {
      case scala.meta.Source(terms) =>
        evalTrees(terms)
      case UnapplyModule(v) => Some(v)
      case UnapplyVal(v) => Some(v)
      case Term.ApplyInfix(x, _, _, xs) => evalTrees(x :: xs)
      case Defn.Val(
            Nil,
            List(Pat.Var(Term.Name(name))),
            None,
            Term.NewAnonymous(Template(_, _, _, xs))
          ) =>
        evalTreesNamed(name, xs)
      case Defn.Val(
            _,
            List(Pat.Var(Term.Name(name))),
            _,
            term
          ) =>
        evalTreesNamed(name, term :: Nil)
      case UnapplyCollectionApply(terms) =>
        evalTrees(terms)
      case Defn.Object(_, Term.Name(name), Template(_, _, _, xs)) =>
        evalTreesNamed(name, xs)
      case _ => None
    }

  def parse(sources: Vector[Source]): Vector[Module] =
    loopReduce(Scope(name = None, items = sources.flatMap(eval)))
      .map(t => substituteVersionTree(Map.empty, t))
      .collect {
        case Scope(_, items) =>
          items.collect { case m: Module => m }
        case m: Module => Vector(m)
      }
      .getOrElse(Vector.empty[Module])

  def moduleToArtifact(scalaVersion: ScalaVersion, m: Module): Artifact =
    m match {
      case Module(
            LitString(groupId),
            percentsCount,
            LitString(artifactId),
            version,
            terms
          ) =>
        Artifact(
          groupId = GroupId(groupId),
          artifactId = if (percentsCount > 1) s"$artifactId%%" else artifactId,
          maybeVersion = version match {
            case LitString(value) => Version(value).some
            case TermNameCompound(_) => None // TODO think what to do
          },
          maybeScalaVersion = if (percentsCount > 1) {
            scalaVersion.some
          } else None,
          scope = terms
            .find {
              case Term.Name("Test") => true
              case _ => false
            }
            .map(_ => ru.d10xa.jadd.core.Scope.Test)
        )
    }

  type WithCounter[A] = (Int, A)

  type ValuesMap = Map[Vector[String], String]

  implicit class WithCounterOps[A](wc: Vector[WithCounter[A]]) {
    def sumCounter: WithCounter[Vector[A]] = wc.separate.leftMap(_.sum)
  }
}
