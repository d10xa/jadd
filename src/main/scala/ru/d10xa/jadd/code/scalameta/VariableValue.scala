package ru.d10xa.jadd.code.scalameta

import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.UnapplySelect

import java.nio.file.Path
import scala.meta.Lit
import scala.meta.Term
import scala.meta.Tree
import scala.meta.inputs.Position

sealed trait VariableValue {
  def withPath(path: Path): VariableValue = this match {
    case lit: VariableLit => VariableLitP(lit, path)
    case x => x
  }

  def existsValue(f: String => Boolean): Boolean = this match {
    case VariableLit(value, _) => f(value)
    case VariableLitP(VariableLit(value, _), _) => f(value)
    case VariableTerms(_) => false
  }

  def valueOption: Option[String] = this match {
    case VariableLit(value, _) => Some(value)
    case VariableLitP(VariableLit(value, _), _) => Some(value)
    case VariableTerms(_) => None
  }
}

/** Example:
  *   "org.typelevel" %% "cats-effect" % "2.5.0"
  *
  * There are three VariableLit found. 1) org.typelevel 2) cats-effect 3) 2.5.0
  */
final case class VariableLit(value: String, pos: Position) extends VariableValue

/** Combination of VariableLit and Path
  */
final case class VariableLitP(lit: VariableLit, path: Path)
    extends VariableValue

/** Example:
  *   "org.scalatest" %% "scalatest" % dependencies.test.scalatest
  * There are only one VariableTerms found
  *   - Vector("dependencies", "test", "scalatest")
  */
final case class VariableTerms(values: Vector[String]) extends VariableValue

object VariableValue {
  def unapply(t: Tree): Option[VariableValue] = t match {
    case lit @ Lit.String(value) => Some(VariableLit(value, lit.pos))
    case Term.Name(value) => Some(VariableTerms(Vector(value)))
    case UnapplySelect(strings) => Some(VariableTerms(strings))
    case _ => None
  }
}
