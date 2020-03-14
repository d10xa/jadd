package ru.d10xa.jadd.core

import cats.ApplicativeError
import cats.FlatMap
import cats.MonadError
import cats.Show
import coursier.core.Version
import eu.timepit.refined.api.RefType
import eu.timepit.refined.api.Refined
import eu.timepit.refined.api.Validate
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.types.string.NonEmptyString
import cats.implicits._
import monocle.Prism
import monocle.macros.GenPrism
import ru.d10xa.jadd.fs.FileOps

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
  object ApplicativeThrowable {
    def apply[F[_]](implicit applicativeError: ApplicativeError[F, Throwable])
      : ApplicativeThrowable[F] =
      applicativeError
  }

  type MonadThrowable[F[_]] = MonadError[F, Throwable]

  object MonadThrowable {
    def apply[F[_]](
      implicit monadThrowable: MonadError[F, Throwable]): MonadThrowable[F] =
      monadThrowable
  }

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

    object TextFile {
      def make[F[_]](fsItem: FsItem)(
        implicit a: ApplicativeThrowable[F]): F[TextFile] =
        fsItem match {
          case t: TextFile => a.pure(t)
          case _: Dir =>
            a.raiseError[TextFile](
              new IllegalArgumentException(s"Is not a file")
            )
          case FileNotFound =>
            a.raiseError[TextFile](
              new IllegalArgumentException("File not found")
            )
        }
    }

    final case class Dir(names: List[FileName]) extends FsItem
    final case object FileNotFound extends FsItem

    val textFilePrism: Prism[FsItem, TextFile] =
      GenPrism[FsItem, TextFile]

    def fromFileNameString[F[_]: ApplicativeThrowable: FlatMap](
      fileName: String,
      fileOps: FileOps[F]): F[FsItem] =
      FileName
        .make[F](fileName)
        .flatMap(fileName => fileOps.read(fileName))
  }

  @newtype case class FileName(value: NonEmptyString)

  object FileName {
    import eu.timepit.refined.collection._
    def make[F[_]: ApplicativeThrowable](name: String): F[FileName] =
      refineF[F, NonEmpty, String](name)
        .map(FileName(_))
  }

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
