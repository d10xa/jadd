package ru.d10xa.jadd.cli

import ru.d10xa.jadd.cli.Command.Repl

case class Config(
  usage: String = "",
  command: Command = Repl,
  artifacts: Seq[String] = Seq.empty,
  projectDir: String = System.getProperty("user.dir"),
  shortcutsUri: String = "classpath:jadd-shortcuts.csv",
  repositories: Seq[String] = Seq.empty,
  dryRun: Boolean = false,
  quiet: Boolean = false,
  debug: Boolean = false
)
