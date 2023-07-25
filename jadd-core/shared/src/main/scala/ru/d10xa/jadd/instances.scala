package ru.d10xa.jadd

import cats.Show

import java.nio.file.Path

object instances {

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val catsShowPath: Show[Path] = _.toString

}
