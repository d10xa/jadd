package ru.d10xa.jadd.code.scalameta

import scala.meta.Source

trait SbtModuleIdFinder {
  def find(source: Source): List[ModuleId]
}

object SbtModuleIdFinder extends SbtModuleIdFinder {

  override def find(source: Source): List[ModuleId] =
    ScalametaUtils.collectNoDuplicate(source, {
      case ModuleIdMatch(m) => m
    })

}
