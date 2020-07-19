package ru.d10xa.jadd.code.scalameta

import scala.meta.Lit
import scala.meta.Term

sealed trait ModuleVersion

/**
  * @param version for example "0.1.0-SNAPSHOT"
  */
final case class VersionString(version: String) extends ModuleVersion

/**
  * @param value for example scalaTestVersion
  */
final case class VersionVal(value: String) extends ModuleVersion

object ModuleVersionMatch {
  def unapply(t: Term): Option[ModuleVersion] = t match {
    case Lit.String(v) => Some(VersionString(v))
    case Term.Name(v) => Some(VersionVal(v))
    case _ => None
  }
}
