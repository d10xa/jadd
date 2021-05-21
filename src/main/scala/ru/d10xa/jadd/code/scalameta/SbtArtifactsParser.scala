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

import java.nio.file.Path
import scala.annotation.tailrec
import scala.meta.Defn
import scala.meta.Pat
import scala.meta.Template
import scala.meta.inputs.Position

trait SbtArtifactsParser[F[_]] {
  def parse(
    sources: Vector[(Path, Source)]
  ): F[Vector[Module]]
  def parseArtifacts(
    scalaVersion: ScalaVersion,
    sources: Vector[(Path, Source)]
  ): F[Vector[Artifact]]
}

object SbtArtifactsParser {

  def make[F[_]: Applicative](): F[SbtArtifactsParser[F]] =
    Applicative[F].pure {
      new SbtArtifactsParser[F] {
        override def parse(sources: Vector[(Path, Source)]): F[Vector[Module]] =
          SbtArtifactsParser
            .parse(SbtArtifactsParser.makeRootScope(sources))
            .pure[F]

        override def parseArtifacts(
          scalaVersion: ScalaVersion,
          sources: Vector[(Path, Source)]
        ): F[Vector[Artifact]] =
          parse(sources).map(_.map(r => moduleToArtifact(scalaVersion, r)))
      }
    }

  def substituteVersion(
    valuesMap: ValuesMap,
    module: Module
  ): Option[Module] =
    module.version match {
      case TermNameCompound(termNames) =>
        valuesMap.get(termNames) match {
          case Some((value, pos)) =>
            Some(module.copy(version = LitString(value, pos)))
          case None => None
        }
      case _ => None
    }

  def createValuesMap(values: Vector[Value]): ValuesMap =
    values.map { case Value(path, value, pos) => (path, (value, pos)) }.toMap

  def separateValues(trees: Vector[SbtTree]): (Vector[Value], Vector[SbtTree]) =
    trees.foldRight((Vector.empty[Value], Vector.empty[SbtTree])) {
      case (v @ Value(_, _, _), (values, trees)) => (v +: values, trees)
      case (t: SbtTree, (values, trees)) => (values, t +: trees)
    }

  def substituteVersionTree(
    valuesMap: ValuesMap,
    tree: SbtTree
  ): SbtTree =
    tree match {
      case scope: Scope =>
        val (values, others) = separateValues(scope.items)
        val newValuesMap = valuesMap ++ createValuesMap(values)
        val newItems = others.map {
          case m @ Module(_, _, _, TermNameCompound(values), _) =>
            newValuesMap.get(values) match {
              case Some((value, pos)) => m.copy(version = LitString(value, pos))
              case None => m
            }
          case scope: Scope =>
            scope.copy(items =
              scope.items.map(i => substituteVersionTree(newValuesMap, i))
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
        case scope @ Scope(Some(innerName), innerItems, _) =>
          val (values, others) = innerItems.partition(_.isInstanceOf[Value])
          val extractedValues =
            values.collect { case v: Value => v.prependPath(innerName) }
          val scopeWithoutValues = scope.copy(items = others)
          (extractedValues.size, (extractedValues, scopeWithoutValues))
        case x => (0, (Vector.empty[Value], x))
      }
      .sumCounter
      .map(appendTree)

  def extractModules(items: Vector[SbtTree]): WithCounter[Vector[SbtTree]] =
    items
      .map {
        case scope: Scope if scope.name.isDefined =>
          val (modules, others) =
            scope.items.partition(_.isInstanceOf[Module])
          val extractedModules = modules.collect { case m: Module => m }
          val newScope = scope.copy(items = others)
          (
            extractedModules.size,
            (extractedModules, newScope)
          )
        case x => (0, (Vector.empty[Module], x))
      }
      .sumCounter
      .map(appendTree)

  def substituteNearModuleWithLocalValue(
    items: Vector[SbtTree]
  ): WithCounter[Vector[SbtTree]] = {
    val localValues = items
      .collect { case v @ Value(_, _, _) => v }
      .foldLeft(Map.empty[Vector[String], (String, Position)])((acc, cur) =>
        acc + ((cur.path, (cur.value, cur.pos)))
      )
    items.map {
      case m @ Module(_, _, _, TermNameCompound(values), _) =>
        localValues.get(values) match {
          case Some((value, pos)) =>
            0 -> m.copy(version = LitString(value, pos))
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
    case scope: Scope if scope.items.isEmpty => 1 -> None
    case Scope(None, Vector(item), path) =>
      1 -> Some(item.withFilePathOpt(path))
    case scope: Scope =>
      innerEval(scope.items) match {
        case (i, value) => i -> Some(scope.copy(items = value))
      }
  }

  def evalTrees(trees: List[scala.meta.Tree]): Option[SbtTree] =
    Scope.makeNonEmpty(
      name = none,
      trees = trees.flatMap(metaTreeToSbtTree).toVector
    )

  def evalTreesNamed(
    name: String,
    trees: List[scala.meta.Tree]
  ): Option[SbtTree] =
    Scope.makeNonEmpty(name.some, trees.flatMap(metaTreeToSbtTree).toVector)

  def metaTreeToSbtTree(tree: scala.meta.Tree): Option[SbtTree] =
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
      case Term.Block(stats) =>
        evalTrees(stats)
      case _ => None
    }

  def findAllModules(t: SbtTree): Vector[Module] = t match {
    case scope: Scope =>
      scope.items.flatMap {
        case m: Module => Vector(m)
        case scope: Scope => scope.items.flatMap(findAllModules)
        case Value(_, _, _) =>
          throw new IllegalStateException(
            s"This branch is not reachable. findAllModules" +
              s" must be executed after substituteVersionTree"
          )
      }
    case m: Module => Vector(m)
  }

  def fileTreesToItems(
    trees: Vector[(Path, scala.meta.Tree)]
  ): Vector[SbtTree] =
    trees.flatMap { case (path, tree) =>
      metaTreeToSbtTree(tree)
        .map(_.withFilePath(path))
    }

  def makeRootScope(trees: Vector[(Path, scala.meta.Tree)]): Scope =
    Scope(name = None, items = fileTreesToItems(trees), filePath = None)

  def parse(rootScope: Scope): Vector[Module] =
    loopReduce(rootScope)
      .map(t => substituteVersionTree(Map.empty, t))
      .map(findAllModules)
      .getOrElse(Vector.empty[Module])

  def moduleToArtifact(scalaVersion: ScalaVersion, m: Module): Artifact =
    m match {
      case Module(
            LitString(groupId, _),
            percentsCount,
            LitString(artifactId, _),
            version,
            terms
          ) =>
        Artifact(
          groupId = GroupId(groupId),
          artifactId = if (percentsCount > 1) s"$artifactId%%" else artifactId,
          maybeVersion = version match {
            case LitString(value, _) => Version(value).some
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
      case _ =>
        throw new IllegalStateException(
          s"This branch is not reachable. LitString is only possible" +
            s" case for groupId and artifactId(at least now)"
        )
    }

  type WithCounter[A] = (Int, A)

  type ValuesMap = Map[Vector[String], (String, Position)]

  implicit class WithCounterOps[A](wc: Vector[WithCounter[A]]) {
    def sumCounter: WithCounter[Vector[A]] = wc.separate.leftMap(_.sum)
  }
}
