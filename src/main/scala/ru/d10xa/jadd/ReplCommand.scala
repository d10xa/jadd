package ru.d10xa.jadd

import java.util

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
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.Try

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

    def readLine(): String = reader.readLine(prompt, rightPrompt, null.asInstanceOf[MaskingCallback], null)
  }

  class JaddCompleter extends org.jline.reader.Completer {

    private val replCommands = Seq("install", "search", "show", "help", "exit")
    private lazy val deps = new ArtifactShortcuts().shortcuts.values

    override def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit = {
      if (line.wordIndex == 0) {
        replCommands.foreach(c => candidates.add(new Candidate(c)))
      }
      line.words().asScala.toList match {
        case x :: xs if x == "install" || x == "search" =>
          deps.foreach(d => candidates.add(new Candidate(d)))
        case _ =>
      }
    }
  }

  def runRepl(action: Array[String] => Unit): Unit = {
    logger.info("Welcome to jadd REPL!")
    val replContext = new ReplContext
    var running = true
    while (running) {
      val line: String = readReplString(replContext)
      running = needContinue(line)
      if(running) {
        action(line.split(" "))
      }
    }
  }

  def needContinue(line: String): Boolean = line.trim != "exit" && line.trim != "quit"

  def readReplString(replContext: ReplContext): String =
    Try(replContext.readLine())
      .recover { case _: UserInterruptException => "exit" } // Ctrl + C
      .recover { case _: EndOfFileException => "exit" } // Ctrl + D
      .get

}
