package ru.d10xa.jadd

import ru.d10xa.jadd.Cli.Config
import ru.d10xa.jadd.Cli.Help
import ru.d10xa.jadd.pipelines.GradlePipeline
import ru.d10xa.jadd.pipelines.MavenPipeline
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder

object Main {

  def run(config: Config): Unit = {
    implicit val artifactInfoFinder: ArtifactInfoFinder =
      new ArtifactInfoFinder(
        Utils.sourceFromSpringUri(config.shortcutsUri)
      )

    val pipelines: List[Pipeline] = List(
      new GradlePipeline(Ctx(config)),
      new MavenPipeline(Ctx(config)),
      new SbtPipeline(Ctx(config))
    )

    pipelines.filter(_.applicable).foreach(_.run())
  }

  def main(args: Array[String]): Unit = {
    Cli.parser.parse(args, Cli.Config()) match {
      case Some(config) if config.command == Help =>
        Cli.parser.showUsage()
      case Some(config) =>
        run(config)
      case None =>
        println("arguments are bad")
    }
  }

}
