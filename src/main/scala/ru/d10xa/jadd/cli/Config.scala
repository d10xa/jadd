package ru.d10xa.jadd.cli

import ru.d10xa.jadd.cli.Command.Repl

case class Config(
  usage: String = "",
  command: Command = Repl,
  artifacts: Seq[String] = Seq.empty,
  projectDir: String = System.getProperty("user.dir"),
  shortcutsUri: String = "classpath:jadd-shortcuts.csv",
  repositories: Seq[String] = Seq(
    "https://jcenter.bintray.com",
    // TODO get repository path from ~/.m2/settings.xml or use default
    // TODO support for ~
    s"${System.getProperty("user.home")}/.m2/repository"
  ),
  dryRun: Boolean = false,
  quiet: Boolean = false,
  debug: Boolean = false
)
