package ru.d10xa.jadd.stringinterpolation

import ru.d10xa.jadd.regex.GradleVerbalExpressions.validVariableNameRegex
import ru.lanwen.verbalregex.VerbalExpression

import scala.collection.JavaConverters.asScalaBufferConverter

final class GStr(str: String) {

  def placeholders(): Set[String] = {
    val re1 = VerbalExpression
      .regex()
      .find("$")
      .find("{")
      .capt()
      .add(validVariableNameRegex)
      .endCapt()
      .find("}")
      .build()
    val re2 = VerbalExpression
      .regex()
      .find("$")
      .capt()
      .add(validVariableNameRegex)
      .endCapt()
      .build()
    val result: Set[String] =
      re1.getTextGroups(str, 1).asScala.toSet ++
        re2.getTextGroups(str, 1).asScala.toSet
    result
  }
  def resolve(m: Map[String, String]): String = {
    val ps = placeholders()
    def replace(str: String, old: String, replacement: String): String = {
      val ve = VerbalExpression
        .regex()
        .find("$")
        .maybe("{")
        .add(old)
        .maybe("}")
        .build()
      val r = ve.toString.r
      r.replaceAllIn(str, replacement)
    }
    ps.foldLeft(str) {
      case (acc, placeholder) =>
        m.get(placeholder) match {
          case Some(value) =>
            replace(acc, placeholder, value)
          case None => acc
        }
    }
  }
}

object GStr {

  def interpolate(m: Map[String, String]): Map[String, String] = {

    def interpolateStep(m: Map[String, String]): Map[String, String] = {
      val t = m.partition(_._2.contains("$"))
      val (gstrs, strs) = t
      val newMap = gstrs
        .mapValues(new GStr(_))
        .mapValues(_.resolve(strs))
      strs ++ newMap
    }

    var currentMap = m
    var hasChanges = true
    do {
      val newMap = interpolateStep(currentMap)
      hasChanges = newMap != currentMap
      currentMap = newMap
    } while (hasChanges)
    currentMap
  }

}
