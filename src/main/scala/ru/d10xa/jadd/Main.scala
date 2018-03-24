package ru.d10xa.jadd

import ru.d10xa.jadd.Cli.Config
import ru.d10xa.jadd.pipelines.GradlePipeline
import ru.d10xa.jadd.pipelines.MavenPipeline
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.pipelines.SbtPipeline

object Main {

  def run(config: Config): Unit = {
    val pipelines: List[Pipeline] = List(
      GradlePipeline(Ctx(config)),
      MavenPipeline(Ctx(config)),
      SbtPipeline(Ctx(config))
    )

    pipelines.filter(_.applicable).foreach(_.run())
  }

  def main(args: Array[String]): Unit = {
    Cli.parser.parse(args, Cli.Config()) match {
      case Some(config) =>
        run(config)
      case None =>
        println("arguments are bad")
    }
  }

}
