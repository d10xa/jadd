package ru.d10xa.jadd.core

import cats.Show
import coursier.core.Version
import io.estatico.newtype.macros.newtype

import scala.language.implicitConversions

/**
  * Warnings disabled because of @newtype
  */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.FinalCaseClass",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
    "org.wartremover.warts.ImplicitConversion"
  ))
object types {

  @newtype case class GroupId(value: String) {
    def path: String =
      value.replace('.', '/') //refineMV(value.value.replace('.', '/'))
  }
  object GroupId {
    implicit val showGroupId: Show[GroupId] = Show[GroupId](_.value.toString)
  }

  @newtype case class ScalaVersion(version: Version)
  object ScalaVersion {
    def fromString(str: String): ScalaVersion = ScalaVersion(Version(str))
    implicit val showScalaVersion: Show[ScalaVersion] =
      Show[ScalaVersion](_.version.repr)
  }

  @newtype case class FileName(value: String)
  @newtype case class FileCache(value: Map[FileName, String])

  object FileCache {
    val empty: FileCache = FileCache(Map.empty[FileName, String])
  }

}
