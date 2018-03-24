package ru.d10xa.jadd

import ru.d10xa.jadd.pipelines.GradlePipeline
import ru.d10xa.jadd.pipelines.MavenPipeline
import ru.d10xa.jadd.pipelines.Pipeline
import ru.d10xa.jadd.pipelines.SbtPipeline
import Cli.Config

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

    //    artifactsWithVersions.foreach { artifact =>
    //      println(s"compile '${artifact.groupId}:${artifact.artifactId}:${artifact.version}'")
    //      println(s"""libraryDependencies += "${artifact.groupId}" % "${artifact.artifactId}" % "${artifact.version}"""")
    //      println(
    //        s"""<dependency>
    //          |    <groupId>${artifact.groupId}</groupId>
    //          |    <artifactId>${artifact.artifactId}</artifactId>
    //          |    <version>${artifact.version}</version>
    //          |</dependency>""".stripMargin)
    //    }

  }

}
