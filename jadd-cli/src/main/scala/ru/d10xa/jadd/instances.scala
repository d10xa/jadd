package ru.d10xa.jadd

import java.nio.file.Path

import cats.Show

object instances {

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val catsShowPath: Show[Path] = _.toString

}
