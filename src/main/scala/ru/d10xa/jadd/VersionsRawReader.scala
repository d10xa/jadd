package ru.d10xa.jadd

trait VersionsRawReader[T] {
  def readVersions(value: T): List[String]
}

object VersionsRawReader {
  def apply[T](f: T => List[String]):VersionsRawReader[T] = (value: T) => f(value)
}
