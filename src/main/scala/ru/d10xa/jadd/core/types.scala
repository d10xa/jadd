package ru.d10xa.jadd.core

import cats.ApplicativeError
import cats.Show
import coursier.core.Version
import eu.timepit.refined.api.RefType
import eu.timepit.refined.api.Refined
import eu.timepit.refined.api.Validate
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.types.string.NonEmptyString
import cats.implicits._
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

  type ApplicativeThrowable[F[_]] = ApplicativeError[F, Throwable]

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

  @newtype case class FileContent(value: String)

  sealed trait FsItem

  object FsItem {
    final case class TextFile(content: FileContent) extends FsItem
    final case class Dir(names: List[FileName]) extends FsItem
    final case object FileNotFound extends FsItem
  }

  @newtype case class FileName(value: NonEmptyString)
  @newtype case class FileCache(value: Map[FileName, FsItem])

  object FileCache {
    val empty: FileCache = FileCache(Map.empty[FileName, FsItem])
  }

  def refineF[F[_]: ApplicativeThrowable, P, T](p: T)(
    implicit v: Validate[T, P]): F[Refined[T, P]] =
    ApplicativeError[F, Throwable].fromEither(
      RefType.refinedRefType
        .refine[P](p)
        .leftMap(s => new IllegalArgumentException(s))
        .leftWiden[Throwable])

}
