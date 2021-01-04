package ru.d10xa.jadd.cli

import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.show.JaddFormatShowPrinter
import ru.d10xa.jadd.show.ShowPrinter
import scopt.OptionDef
import scopt.OptionParser

trait Cli {
  def parse(args: Vector[String]): Config
}

object Cli extends Cli {

  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("jadd") {

    override def showUsageOnError: Boolean = false

    head("jadd", Ctx.version)

    def multipleArtifacts: OptionDef[String, Config] =
      arg[String]("<artifact>...")
        .text("unbounded args")
        .action((x, c) => c.copy(artifacts = c.artifacts :+ x))
        .unbounded()
        .optional()

    opt[Unit]('q', "quiet")
      .text("log errors only")
      .action((_, c) => c.copy(quiet = true))

    opt[Unit]("debug")
      .text("print debug messages")
      .action((_, c) => c.copy(debug = true))

    opt[String]('p', "project-dir")
      .text("Specifies the project directory. Defaults to current directory.")
      .action((x, c) => c.copy(projectDir = x))

    opt[Seq[String]]('r', "requirements")
      .text("Install from the given requirements file/url")
      .action((x, c) => c.copy(requirements = x))

    opt[String]("scala-version")
      .optional()
      .text("Define scala version for %% resolution")
      .action((x, c) => c.copy(scalaVersion = Some(ScalaVersion.fromString(x))))

    opt[Seq[String]]("repository")
      .action((x, c) => c.copy(repositories = x))

    opt[String]("proxy")
      .text(
        "http proxy. (Format http://host:port or http://user:password@host:port)"
      )
      .action((x, c) => c.copy(proxy = Some(x)))

    opt[String]("shortcuts-uri")
      .text("Specifies uri for artifacts shortcuts csv file")
      .action((x, c) => c.copy(shortcutsUri = x))

    // TODO generate list of formats automatically
    opt[String]('f', "output-format")
      .text(
        "artifacts output format (ammonite, gradle, groovy, leiningen, maven, mill, sbt, jadd, jadd-no-versions)"
      )
      .action((x, c) =>
        c.copy(
          showPrinter = ShowPrinter
            .fromString(x)
            .getOrElse(JaddFormatShowPrinter.withVersions)
        )
      )

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

    cmd("help")
      .text("prints this usage text")
      .action((_, c) => c.copy(command = Help))
  }

  override def parse(args: Vector[String]): Config = {
    val config = parser
      .parse(args, Config.empty)
      .getOrElse(Config.empty.copy(command = Help))
    config.copy(usage = parser.usage)
  }

}
