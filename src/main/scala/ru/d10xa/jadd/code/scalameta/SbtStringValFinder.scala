package ru.d10xa.jadd.code.scalameta

import scala.meta.Source

trait SbtStringValFinder {
  def find(source: Source): List[StringVal]
}

object SbtStringValFinder extends SbtStringValFinder {
  override def find(source: Source): List[StringVal] =
    ScalametaUtils.collectNoDuplicate(
      source,
      { case StringValMatch(m) =>
        m
      }
    )
}
