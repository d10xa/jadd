package ru.d10xa.jadd.repl

import java.util
import cats.effect.IO
import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.show.ShowPrinter

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.util.control.NonFatal

class JaddJlineCompleter[F[_]] extends org.jline.reader.Completer {

  private val commandsNeedCompletion = Set("install", "search", "i", "s")
  private val replCommands = Seq("install", "search", "show", "help", "exit")

  // TODO Make ability to specify source of shortcuts
  private val shortcuts: Map[String, String] =
    ArtifactShortcuts.ArtifactShortcutsClasspath.shortcuts

  private val autocomplete = new ReplAutocomplete(
    new ArtifactAutocompleteCache(
      mutable.Set(shortcuts.values.toSeq: _*)
    )
  )

  override def complete(
    reader: LineReader,
    line: ParsedLine,
    candidates: util.List[Candidate]
  ): Unit = {
    val words: Seq[String] = line.words().asScala.toList
    val autocompleteFormat =
      JaddJlineCompleter.matchFormatOutput(words)
    if (autocompleteFormat) {
      candidates.clear()
      ShowPrinter.printerNames.map(new Candidate(_)).foreach(candidates.add)
    } else if (line.wordIndex == 0) {
      replCommands.foreach(c => candidates.add(new Candidate(c)))
    } else {
      implicit val runtime = cats.effect.unsafe.IORuntime.global
      candidates.clear()
      val word = line.word()
      words.headOption.toList
        .flatMap { command =>
          if (commandsNeedCompletion.contains(command)) {
            autocomplete
              .complete(word)
              .handleErrorWith {
                case e: org.jsoup.HttpStatusException
                    if e.getStatusCode == 404 =>
                  IO.pure(Vector.empty)
                case NonFatal(e) =>
                  IO(
                    reader.getTerminal.writer
                      .println(s"autocomplete error: ${e.toString}")
                  ) *> IO.pure(Vector.empty)
              }
              .unsafeRunSync()
          } else Vector.empty
        }
        .map(new Candidate(_))
        .foreach(candidates.add)
    }
  }
}

object JaddJlineCompleter {

  def matchOpt(anyArgs: String*)(words: String*): Boolean =
    words
      .filter(_.nonEmpty)
      .reverse
      .take(2)
      .filter(anyArgs.contains(_))
      .lengthCompare(1) == 0

  val matchFormatOutput: Seq[String] => Boolean =
    matchOpt("-f", "--output-format")

}
