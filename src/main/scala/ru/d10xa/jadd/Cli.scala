package ru.d10xa.jadd

import scopt.OptionParser

object Cli {
  case class Config(
    artifacts: Seq[String] = Seq.empty,
    projectDir: String = System.getProperty("user.dir")
  )
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("jadd") {
    head("jadd", "SNAPSHOT")
    arg[String]("<artifact>...").unbounded().optional().action((x, c) =>
      c.copy(artifacts = c.artifacts :+ x)).text("unbounded args")
    opt[String]('p', "project-dir").action((x, c) =>
      c.copy(projectDir = x)).text("Specifies the project directory. Defaults to current directory.")
  }
}
