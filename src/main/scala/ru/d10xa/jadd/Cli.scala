package ru.d10xa.jadd

import scopt.OptionParser

object Cli {
  sealed trait Command
  case object Install extends Command
  case object Search extends Command
  case object Help extends Command

  case class Config(
    command: Command = Help,
    artifacts: Seq[String] = Seq.empty,
    projectDir: String = System.getProperty("user.dir"),
    shortcutsUri: String = "https://github.com/d10xa/jadd/raw/master/src/main/resources/jadd-shortcuts.csv",
    dryRun: Boolean = false
  )
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("jadd") {
    head("jadd", "SNAPSHOT")
    arg[String]("<artifact>...").unbounded().optional().action((x, c) =>
      c.copy(artifacts = c.artifacts :+ x)).text("unbounded args")
    opt[Unit]("dry-run").action((_, c) =>
      c.copy(dryRun = true)).text("read-only mode")
    opt[String]('p', "project-dir").action((x, c) =>
      c.copy(projectDir = x)).text("Specifies the project directory. Defaults to current directory.")
    opt[String]("shortcuts-uri").action((x, c) =>
      c.copy(shortcutsUri = x)).text("Specifies uri for artifacts shortcuts csv file" +
      " (default https://github.com/d10xa/jadd/raw/master/src/main/resources/jadd-shortcuts.csv)")

    cmd("install")
      .text("install dependency to build file")
      .action((_, c) => c.copy(command = Install))

    cmd("i")
      .text("alias for install")
      .action((_, c) => c.copy(command = Install))

    cmd("search")
      .text("search dependency in shortcuts")
      .action((_, c) => c.copy(command = Search))

    cmd("s")
      .text("alias for search")
      .action((_, c) => c.copy(command = Search))

    cmd("help")
      .text("prints this usage text")
      .action((_, c) => c.copy(command = Help))
  }
}
