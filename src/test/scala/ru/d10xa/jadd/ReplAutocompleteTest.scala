package ru.d10xa.jadd

import org.jsoup.Jsoup
import org.scalatest.FunSuite

import scala.io.Source

class ReplAutocompleteTest extends FunSuite {

  test("testParseMavenCentralHtml") {

    val string =
      Source
        .fromResource("central.maven.org/maven2/junit/index.html")
        .mkString

    val names: Vector[String] =
      ReplAutocomplete.parseMavenCentralHtml(Jsoup.parse(string))

    assert(names === List("junit", "junit-dep"))
  }

}
