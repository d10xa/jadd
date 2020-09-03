package ru.d10xa.jadd.repl

import cats.effect._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.d10xa.jadd.repository.RepositoryConstants

import scala.jdk.CollectionConverters._

class ReplAutocomplete(val cache: ArtifactAutocompleteCache) {
  import ReplAutocomplete._

  def complete(word: String): IO[Vector[String]] =
    if (word.endsWith(":") && !cache.deps.contains(word.dropRight(1))) {
      for {
        candidates <- fetchCandidates(
          RepositoryConstants.mavenCentral,
          word.dropRight(1))
        _ <- cache.cache(word.dropRight(1), candidates)
      } yield candidates
    } else {
      IO {
        cache.deps
          .filter(_.contains(word))
          .toVector
          .sorted
      }
    }
}

object ReplAutocomplete {

  def fetchJsoupDocument(url: String): IO[Document] = IO {
    Jsoup.connect(url).get()
  }

  def parseMavenCentralHtml(jsoupDocument: Document): Vector[String] =
    jsoupDocument
      .select("a")
      .asScala
      .toList
      .filterNot(_.attr("href") == "../")
      .map(_.text().dropRight(1))
      .toVector

  def autocompleteCandidatesFromDocument(
    d: Document,
    completeModulePart: String): Vector[String] =
    parseMavenCentralHtml(d)
      .map(s => s"$completeModulePart:$s")

  def autocompleteCandidateAsPath(completeModulePart: String): String =
    completeModulePart
      .split("[:.]")
      .mkString("/")

  /**
    *
    * @param completeModulePart groupId or groupId:artifactId
    * @return
    */
  def fetchCandidates(
    repository: String,
    completeModulePart: String): IO[Vector[String]] = {
    val ioDoc: IO[Document] = fetchJsoupDocument(
      s"$repository/${autocompleteCandidateAsPath(completeModulePart)}")
    ioDoc.map(autocompleteCandidatesFromDocument(_, completeModulePart))
  }

}
