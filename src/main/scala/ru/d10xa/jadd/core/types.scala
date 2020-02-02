package ru.d10xa.jadd.core

import cats.Show
//import eu.timepit.refined._
//import eu.timepit.refined.types.string.NonEmptyString
//import io.estatico.newtype.macros.newtype

object types {

//  @newtype case class GroupId(value: NonEmptyString) {
//    def path: NonEmptyString = refineMV(value.value.replace('.', '/'))
//  }

  final case class GroupId(val value: String) extends AnyVal {
    def path: String = value.replace('.', '/')
  }

  object GroupId {
    implicit val showGroupId: Show[GroupId] = Show[GroupId](_.value.toString)
  }

}
