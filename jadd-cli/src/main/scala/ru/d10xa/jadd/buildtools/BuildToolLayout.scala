package ru.d10xa.jadd.buildtools

sealed trait BuildToolLayout

object BuildToolLayout {
  object Sbt extends BuildToolLayout
  object Gradle extends BuildToolLayout
  object Maven extends BuildToolLayout
  object Ammonite extends BuildToolLayout
  object Unknown extends BuildToolLayout
}
