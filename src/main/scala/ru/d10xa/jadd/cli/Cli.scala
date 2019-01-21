package ru.d10xa.jadd.cli

import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.cli.Command.Analyze
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.cli.Command.Show
import scopt.OptionDef
import scopt.OptionParser

trait Cli {
  def parse(args: Array[String]): Config
}

object Cli extends Cli {

  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("jadd") {

    override def showUsageOnError: Boolean = false

    head("jadd", Ctx.version)

    def multipleArtifacts: OptionDef[String, Config] = arg[String]("<artifact>...")
      .text("unbounded args")
      .action((x, c) => c.copy(artifacts = c.artifacts :+ x))
      .unbounded()
      .optional()

    opt[Unit]("dry-run")
      .text("read-only mode")
      .action((_, c) => c.copy(dryRun = true))

    opt[Unit]('q', "quiet")
      .text("log errors only")
      .action((_, c) => c.copy(quiet = true))

    opt[Unit]("debug")
      .text("print debug messages")
      .action((_, c) => c.copy(debug = true))

    opt[String]('p', "project-dir")
      .text("Specifies the project directory. Defaults to current directory.")
      .action((x, c) => c.copy(projectDir = x))

    opt[Seq[String]]("repository")
      .action((x, c) => c.copy(repositories = x))

    opt[String]("proxy")
      .text("http proxy. (Format http://host:port or http://user:password@host:port)")
      .action((x, c) => c.copy(proxy = Some(x)))

    opt[String]("shortcuts-uri")
      .text("Specifies uri for artifacts shortcuts csv file")
      .action((x, c) => c.copy(shortcutsUri = x))

    cmd("install")
      .text("install dependency to build file")
      .action((_, c) => c.copy(command = Install))
      .children(multipleArtifacts)

    cmd("i")
      .action((_, c) => c.copy(command = Install))
      .hidden()
      .children(multipleArtifacts)

    cmd("search")
      .text("search dependency in shortcuts")
      .action((_, c) => c.copy(command = Search))
      .children(multipleArtifacts)

    cmd("s")
      .action((_, c) => c.copy(command = Search))
      .hidden()
      .children(multipleArtifacts)

    cmd("show")
      .text("print dependencies")
      .action((_, c) => c.copy(command = Show))

    cmd("analyze")
      .text("search dependency in multiple repositories and print all available versions")
      .action((_, c) => c.copy(command = Analyze))
      .children(multipleArtifacts)

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
