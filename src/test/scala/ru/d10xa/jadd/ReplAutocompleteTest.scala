package ru.d10xa.jadd

import org.jsoup.Jsoup
import org.scalatest.FunSuite
import ru.d10xa.jadd.repl.ReplAutocomplete
import ru.d10xa.jadd.repl.ReplAutocomplete.autocompleteCandidateAsPath
import ru.d10xa.jadd.repl.ReplAutocomplete.autocompleteCandidatesFromDocument

import scala.io.Source

class ReplAutocompleteTest extends FunSuite {

  val string: String =
    Source
      .fromResource("central.maven.org/maven2/junit/index.html")
      .mkString

  test("testParseMavenCentralHtml") {
    val names: Vector[String] =
      ReplAutocomplete.parseMavenCentralHtml(Jsoup.parse(string))

    assert(names === List("junit", "junit-dep"))
  }

  test("autocompleteCandidateAsPath") {
    assert(autocompleteCandidateAsPath("org.junit:") === "org/junit")
  }

  test("autocompleteCandidatesFromDocument") {
    val candidates =
      autocompleteCandidatesFromDocument(Jsoup.parse(string), "org.junit")
    assert(candidates === Vector("org.junit:junit", "org.junit:junit-dep"))
  }

}
