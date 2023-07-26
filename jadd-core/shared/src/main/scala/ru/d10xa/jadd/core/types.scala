package ru.d10xa.jadd.core

import cats.ApplicativeError
import cats.Show
import cats.syntax.all._
import coursier.core.Version
import eu.timepit.refined.api.RefType
import eu.timepit.refined.api.Refined
import eu.timepit.refined.api.Validate

import scala.language.implicitConversions

object types {

  type ApplicativeThrowable[F[_]] = ApplicativeError[F, Throwable]
  object ApplicativeThrowable {
    def apply[F[_]](implicit
      applicativeError: ApplicativeError[F, Throwable]
    ): ApplicativeThrowable[F] =
      applicativeError
  }


  // TODO @newtype
  case class GroupId(value: String) {
    def path: String =
      value.replace('.', '/')
  }
  object GroupId {
    implicit val showGroupId: Show[GroupId] = Show[GroupId](_.value)
  }

  // TODO @newtype
  case class ScalaVersion(version: Version)
  object ScalaVersion {
    def fromString(str: String): ScalaVersion = ScalaVersion(Version(str))
    implicit val showScalaVersion: Show[ScalaVersion] =
      Show[ScalaVersion](_.version.repr)
  }

  // TODO @newtype
  case class FileContent(value: String)

  def refineF[F[_]: ApplicativeThrowable, P, T](
    p: T
  )(implicit v: Validate[T, P]): F[Refined[T, P]] =
    ApplicativeError[F, Throwable].fromEither(
      RefType.refinedRefType
        .refine[P](p)
        .leftMap(s => new IllegalArgumentException(s))
        .leftWiden[Throwable]
    )

}
