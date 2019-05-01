package ru.d10xa.jadd.cli

import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.show.JaddFormatShowPrinter
import ru.d10xa.jadd.show.ShowPrinter

final case class Config(
  usage: String = "",
  command: Command = Repl,
  artifacts: Seq[String] = Seq.empty,
  requirements: Seq[String] = Seq.empty,
  projectDir: String = System.getProperty("user.dir"),
  shortcutsUri: String = "classpath:jadd-shortcuts.csv",
  repositories: Seq[String] = Seq(
    "https://jcenter.bintray.com",
    // TODO get repository path from ~/.m2/settings.xml or use default
    // TODO support for ~
    s"${System.getProperty("user.home")}/.m2/repository"
  ),
  showPrinter: ShowPrinter = JaddFormatShowPrinter,
  proxy: Option[String] = None,
  quiet: Boolean = false,
  debug: Boolean = false
)
