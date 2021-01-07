package ru.d10xa.jadd

import cats.data.NonEmptyList
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import coursier.util.ValidationNel

object extensions {

  implicit class CoursierValidationNelExtension[L, R](
    vnel: ValidationNel[L, R]) {
    def toCatsValidatedNel: Validated[NonEmptyList[L], R] = vnel.either match {
      case Left(h :: t) => Invalid(NonEmptyList(h, t))
      case Right(value) => Valid(value)
    }
  }

  implicit class ValidatedNelStringOps[A](
    vnel: Validated[NonEmptyList[String], A]) {
    import cats.syntax.foldable._
    def joinNel: Validated[String, A] =
      vnel.leftMap {
        case NonEmptyList(head, Nil) => head
        case nel => nel.mkString_("(", ", ", ")")
      }
  }

}
