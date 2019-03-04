package ru.d10xa.jadd

import cats.implicits._
import cats.effect._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.d10xa.jadd.ReplAutocomplete.ArtifactAutocompleteCache
import ru.d10xa.jadd.repository.RepositoryConstants

import scala.collection.mutable
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.Try

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

  final class ArtifactAutocompleteCache(val deps: mutable.Set[String]) {

    private val visitedRemotely: mutable.HashSet[String] =
      mutable.HashSet[String]()

    def cache(completeModulePart: String, v: Vector[String]): IO[Unit] = IO {
      visitedRemotely.add(completeModulePart)
      v.foreach(deps.add)
    }

  }

  def fetchJsoupDocument(url: String): IO[Document] =
    IO.fromEither(Try(Jsoup.connect(url).get()).toEither)

  def parseMavenCentralHtml(jsoupDocument: Document): Vector[String] =
    jsoupDocument
      .select("a")
      .asScala
      .toList
      .filterNot(_.attr("href") == "../")
      .map(_.text().dropRight(1))
      .toVector

  /**
    *
    * @param completeModulePart groupId or groupId:artifactId
    * @return
    */
  def fetchCandidates(
    repository: String,
    completeModulePart: String): IO[Vector[String]] = {
    val y = completeModulePart
      .split("[:.]")
      .mkString("/")
    val ioDoc: IO[Document] = fetchJsoupDocument(s"$repository/$y")
    ioDoc.map(
      d =>
        parseMavenCentralHtml(d)
          .map(s => s"$completeModulePart:$s"))
  }

}
