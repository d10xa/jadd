package ru.d10xa.jadd.core

import cats.Show
//import eu.timepit.refined._
//import eu.timepit.refined.types.string.NonEmptyString
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

//  @newtype case class GroupId(value: NonEmptyString) {
//    def path: NonEmptyString = refineMV(value.value.replace('.', '/'))
//  }

  @newtype case class GroupId(value: String) {
    def path: String =
      value.replace('.', '/') //refineMV(value.value.replace('.', '/'))
  }
  object GroupId {
    implicit val showGroupId: Show[GroupId] = Show[GroupId](_.value.toString)
  }

}
