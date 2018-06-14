package ru.d10xa.jadd.cli

import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.cli.Command.Analyze
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.cli.Command.Show
import scopt.OptionParser

trait Cli {
  def parse(args: Array[String]): Config
}

object Cli extends Cli {

  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("jadd") {
    head("jadd", Ctx.version)
    arg[String]("<artifact>...").unbounded().optional().action((x, c) =>
      c.copy(artifacts = c.artifacts :+ x)).text("unbounded args")
    opt[Unit]("dry-run").action((_, c) =>
      c.copy(dryRun = true)).text("read-only mode")
    opt[Unit]("debug").action((_, c) =>
      c.copy(debug = true)).text("print debug messages")
    opt[String]('p', "project-dir").action((x, c) =>
      c.copy(projectDir = x)).text("Specifies the project directory. Defaults to current directory.")
    opt[Seq[String]]("repository").action((x, c) =>
      c.copy(repositories = x))
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

    cmd("show")
      .text("print dependencies")
      .action((_, c) => c.copy(command = Show))

    cmd("analyze")
      .text("search dependency in multiple repositories and print all available versions")
      .action((_, c) => c.copy(command = Analyze))

    cmd("help")
      .text("prints this usage text")
      .action((_, c) => c.copy(command = Help))
  }

  override def parse(args: Array[String]): Config = {
    val config = parser.parse(args, Config())
      .getOrElse(Config(command = Help))
    config.copy(usage = parser.usage)
  }

}
