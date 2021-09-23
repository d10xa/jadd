package ru.d10xa.jadd.code.scalameta

import cats.Applicative

import scala.meta.Source
import scala.meta.Term
import cats.syntax.all._
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyCollectionApply
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Module
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyModule
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Scope
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.SbtTree
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplyVal
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Value
import ru.d10xa.jadd.code.scalameta.types._

import java.nio.file.Path
import scala.annotation.tailrec
import scala.meta.Defn
import scala.meta.Pat
import scala.meta.Template

trait SbtModuleParser[F[_]] {
  def parse(
    sources: Vector[(Path, Source)]
  ): F[Vector[Module]]
}

object SbtModuleParser {

  import SbtModuleSbtTree.makeRootScope
  import SbtModuleParserLoopReduce.loopReduce
  import SbtModuleSubstituteVersionTree.substituteVersionTree
  import SbtModuleFindModules.findAllModules

  def make[F[_]: Applicative](): F[SbtModuleParser[F]] =
    Applicative[F].pure { (sources: Vector[(Path, Source)]) =>
      SbtModuleParser
        .parse(makeRootScope(sources))
        .pure[F]
    }

  def parse(rootScope: Scope): Vector[Module] =
    loopReduce(rootScope)
      .map(t => substituteVersionTree(Map.empty, t))
      .map(findAllModules)
      .getOrElse(Vector.empty[Module])

}

object SbtModuleSbtTree {
  def makeRootScope(
    trees: Vector[(Path, scala.meta.Tree)]
  ): Scope =
    Scope(name = None, items = fileTreesToItems(trees), filePath = None)

  def evalTrees(trees: List[scala.meta.Tree], path: Path): Option[SbtTree] =
    Scope.makeNonEmpty(
      name = none,
      trees = trees.flatMap(t => metaTreeToSbtTree(t, path)).toVector,
      path
    )

  def evalTreesNamed(
    name: String,
    trees: List[scala.meta.Tree],
    path: Path
  ): Option[SbtTree] =
    Scope.makeNonEmpty(
      name.some,
      trees.flatMap(t => metaTreeToSbtTree(t, path)).toVector,
      path
    )

  def metaTreeToSbtTree(tree: scala.meta.Tree, path: Path): Option[SbtTree] =
    tree match {
      case scala.meta.Source(terms) =>
        evalTrees(terms, path)
      case UnapplyModule(v) => Some(v.withFilePath(path))
      case UnapplyVal(v) => Some(v.withFilePath(path))
      case Term.ApplyInfix(x, _, _, xs) => evalTrees(x :: xs, path)
      case Defn.Val(
            Nil,
            List(Pat.Var(Term.Name(name))),
            None,
            Term.NewAnonymous(Template(_, _, _, xs))
          ) =>
        evalTreesNamed(name, xs, path)
      case Defn.Val(
            _,
            List(Pat.Var(Term.Name(name))),
            _,
            term
          ) =>
        evalTreesNamed(name, term :: Nil, path)
      case UnapplyCollectionApply(terms) =>
        evalTrees(terms, path)
      case Defn.Object(_, Term.Name(name), Template(_, _, _, xs)) =>
        evalTreesNamed(name, xs, path)
      case Term.Block(stats) =>
        evalTrees(stats, path)
      case _ => None
    }

  def fileTreesToItems(
    trees: Vector[(Path, scala.meta.Tree)]
  ): Vector[SbtTree] =
    trees.flatMap { case (path, tree) =>
      metaTreeToSbtTree(tree, path)
    }

}

object SbtModuleParserLoopReduce {

  implicit class WithCounterOps[A](wc: Vector[WithCounter[A]]) {
    def sumCounter: WithCounter[Vector[A]] = wc.separate.leftMap(_.sum)
  }

  def reduceTrees(nodes: Vector[SbtTree]): WithCounter[Vector[SbtTree]] =
    nodes.map(reduceTree).sumCounter.map(_.flatten)

  private def reduceTree(node: SbtTree): WithCounter[Option[SbtTree]] =
    node match {
      case v: Value => 0 -> Some(v)
      case m: Module => 0 -> Some(m)
      case scope: Scope if scope.items.isEmpty => 1 -> None
      case Scope(None, Vector(item), _) =>
        1 -> Some(item)
      case scope: Scope =>
        innerEval(scope) match {
          case (i, value) => i -> Some(scope.copy(items = value))
        }
    }

  private def innerEval(scope: Scope): WithCounter[Vector[SbtTree]] =
    substituteNearModuleWithLocalValue(scope)
      .flatMap(extractValues)
      .flatMap(extractModules)
      .flatMap(SbtModuleParserLoopReduce.reduceTrees)

  def extractValues(
    items: Vector[SbtTree]
  ): WithCounter[Vector[SbtTree]] =
    items
      .map {
        case scope @ Scope(innerNameOpt, innerItems, _) =>
          val (values, others) = innerItems.partition(_.isInstanceOf[Value])
          val extractedValues =
            values.collect { case v: Value =>
              innerNameOpt match {
                case Some(innerName) => v.prependPath(innerName)
                case None => v
              }
            }
          val scopeWithoutValues = scope.copy(items = others)
          (extractedValues.size, (extractedValues, scopeWithoutValues))
        case x => (0, (Vector.empty[Value], x))
      }
      .sumCounter
      .map(appendTree)

  private def appendTree(
    xs: Vector[(Vector[SbtTree], SbtTree)]
  ): Vector[SbtTree] =
    xs.flatMap { case (xs2, x) => xs2 :+ x }

  def extractModules(
    items: Vector[SbtTree]
  ): WithCounter[Vector[SbtTree]] =
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

  private def substituteNearModuleWithLocalValue(
    scope: Scope
  ): WithCounter[Vector[SbtTree]] = {
    val localValues = scope.items
      .collect { case v: Value => v }
      .foldLeft(Map.empty[Vector[String], VariableLitP])((acc, cur) =>
        acc + (
          (
            cur.path,
            VariableLitP(VariableLit(cur.value, cur.pos), cur.filePath)
          )
        )
      )
    scope.items.map {
      case m @ Module(_, _, _, VariableTerms(values), _) =>
        localValues.get(values) match {
          case Some(varLitP) =>
            0 -> m.copy(version = varLitP)
          case None => (0, m)
        }
      case x => (0, x)
    }.sumCounter
  }

  @tailrec
  def loopReduce(node: SbtTree): Option[SbtTree] =
    reduceTree(node) match {
      case (i, Some(tree)) if i > 0 => loopReduce(tree)
      case (_, treeOpt) => treeOpt
      case _ => None
    }

}

object SbtModuleSubstituteVersionTree {

  def substituteVersionTree(
    valuesMap: ValuesMap,
    tree: SbtTree
  ): SbtTree =
    tree match {
      case scope: Scope =>
        // Get values from current scope only
        val (values, others) = separateValues(scope.items)

        // Create new Map based on current scope only
        val newValuesMap = {
          val createdValuesMap = scope.filePath match {
            case Some(value) => createValuesMap(value, values)
            case None => Map.empty[Vector[String], VariableLitP]
          }
          valuesMap ++ createdValuesMap
        }

        // Set values to local modules
        val newItems = values ++ others.map {
          case m @ Module(_, _, _, VariableTerms(values), _) =>
            newValuesMap.get(values) match {
              case Some(varLitP) =>
                m.copy(version = varLitP)
              case None => m
            }
          case scope: Scope =>
            scope.copy(items =
              scope.items.map(i => substituteVersionTree(newValuesMap, i))
            )
          case x => x
        }

        scope.copy(items = newItems)
      case m @ Module(_, _, _, VariableTerms(_), _) =>
        substituteVersion(valuesMap, m).getOrElse(m)
      case _ => tree
    }

  private def createValuesMap(
    filePath: Path,
    values: Vector[Value]
  ): ValuesMap =
    values.collect { case v: Value =>
      (v.path, VariableLitP(VariableLit(v.value, v.pos), filePath))
    }.toMap

  private def separateValues(
    trees: Vector[SbtTree]
  ): (Vector[Value], Vector[SbtTree]) =
    trees.foldRight((Vector.empty[Value], Vector.empty[SbtTree])) {
      case (v: Value, (values, trees)) => (v +: values, trees)
      case (t: SbtTree, (values, trees)) => (values, t +: trees)
    }

  private def substituteVersion(
    valuesMap: ValuesMap,
    module: Module
  ): Option[Module] =
    module.version match {
      case VariableTerms(termNames) =>
        valuesMap.get(termNames) match {
          case Some(varLitP) =>
            Some(module.copy(version = varLitP))
          case None => None
        }
      case _ => None
    }

}

object SbtModuleFindModules {
  def findAllModules(t: SbtTree): Vector[Module] = t match {
    case scope: Scope =>
      scope.items.flatMap {
        case m: Module => Vector(m)
        case scope: Scope => scope.items.flatMap(findAllModules)
        case _: Value => Vector.empty
      }
    case m: Module => Vector(m)
    case _: Value => Vector.empty
  }
}
