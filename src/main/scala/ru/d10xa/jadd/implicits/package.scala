package ru.d10xa.jadd

package object implicits {
  object sbt extends SbtImplicits
  object gradle extends GradleImplicits
  object maven extends MavenImplicits
}
