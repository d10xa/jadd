package ru.d10xa.jadd

import java.util

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.MaskingCallback
import org.jline.reader.ParsedLine
import org.jline.reader.Parser
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import ru.d10xa.jadd.ReplAutocomplete.ArtifactAutocompleteCache
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable
import scala.util.Try
import scala.util.control.NonFatal

object ReplCommand extends StrictLogging {

  class ReplContext(
    val prompt: String = "jadd> ",
    val rightPrompt: String = null,
    val completer: Completer = new JaddCompleter
  ) {

    val builder: TerminalBuilder = TerminalBuilder.builder
    val terminal: Terminal = builder.build
    val parser: Parser = null
    val reader: LineReader =
      LineReaderBuilder.builder
        .terminal(terminal)
        .completer(completer)
        .parser(parser)
        .build

    def readLine(): String =
      reader.readLine(
        prompt,
        rightPrompt,
        null.asInstanceOf[MaskingCallback],
        null)
  }

  class JaddCompleter extends org.jline.reader.Completer {

    private val commandsNeedCompletion = Set("install", "search", "i", "s")
    private val replCommands = Seq("install", "search", "show", "help", "exit")

    val autocomplete = new ReplAutocomplete(
      new ArtifactAutocompleteCache(
        mutable.Set(new ArtifactShortcuts().shortcuts.values.toSeq: _*)
      ))

    override def complete(
      reader: LineReader,
      line: ParsedLine,
      candidates: util.List[Candidate]): Unit =
      if (line.wordIndex == 0) {
        replCommands.foreach(c => candidates.add(new Candidate(c)))
      } else {
        candidates.clear()
        val word = line.word()
        line
          .words()
          .asScala
          .headOption
          .toVector
          .flatMap { command =>
            if (commandsNeedCompletion.contains(command)) {
              autocomplete
                .complete(word)
                .handleErrorWith {
                  case e: org.jsoup.HttpStatusException
                      if e.getStatusCode == 404 =>
                    IO.pure(Vector.empty)
                  case NonFatal(e) =>
                    logger.warn(e.getMessage)
                    IO.pure(Vector.empty)
                }
                .unsafeRunSync()
            } else Vector.empty
          }
          .map(new Candidate(_))
          .foreach(candidates.add)
      }
  }

  def runRepl(action: Array[String] => Unit): Unit = {
    logger.info("Welcome to jadd REPL!")
    val replContext = new ReplContext
    var running = true
    while (running) {
      val line: String = readReplString(replContext)
      running = needContinue(line)
      if (running) {
        action(line.split(" "))
      }
    }
  }

  def needContinue(line: String): Boolean =
    line.trim != "exit" && line.trim != "quit"

  def readReplString(replContext: ReplContext): String =
    Try(replContext.readLine())
      .recover { case _: UserInterruptException => "exit" } // Ctrl + C
      .recover { case _: EndOfFileException => "exit" } // Ctrl + D
      .get

}
