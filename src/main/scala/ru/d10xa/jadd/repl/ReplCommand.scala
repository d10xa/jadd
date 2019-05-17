package ru.d10xa.jadd.repl

import com.typesafe.scalalogging.StrictLogging
import org.jline.reader._
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import ru.d10xa.jadd.RunParams

import scala.util.Try

object ReplCommand extends StrictLogging {

  class ReplContext(
    val prompt: String = "jadd> ",
    val rightPrompt: String = null,
    val completer: Completer = new JaddJlineCompleter
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

  def runRepl(runParams: RunParams, action: RunParams => Unit): Unit = {
    logger.info("Welcome to jadd REPL!")
    val replContext = new ReplContext
    var running = true
    while (running) {
      val line: String = readReplString(replContext)
      running = needContinue(line)
      if (running) {
        action(runParams.copy(args = line.split(" ")))
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
