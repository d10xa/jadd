package ru.d10xa.jadd

import java.io.File

import ru.d10xa.jadd.shortcuts.ArtifactShortcuts

import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {

    val sbtFile = new File("build.sbt")
    val gradleFile = new File("build.gradle")
    val mavenFile = new File("pom.xml")

    val buildFiles = sbtFile :: gradleFile :: mavenFile :: Nil

    buildFiles.filter(_.exists()).foreach(f => println(s"${f.getName} exists"))

    val shortcuts = new ArtifactShortcuts().shortcuts

    val artifacts: Seq[ArtifactWithoutVersion] = args
      .collect {
        case str if shortcuts.contains(str) => shortcuts(str)
        case str =>
          val Array(a, b) = str.split(':')
          ArtifactWithoutVersion(a, b)
      }

    println(artifacts)

    val artifactsWithVersions = artifacts.map(Utils.loadLatestVersion)

    artifactsWithVersions.foreach { artifact =>
      println(s"compile '${artifact.groupId}:${artifact.artifactId}:${artifact.version}'")
      println(s"""libraryDependencies += "${artifact.groupId}" % "${artifact.artifactId}" % "${artifact.version}"""")
      println(
        s"""<dependency>
          |    <groupId>${artifact.groupId}</groupId>
          |    <artifactId>${artifact.artifactId}</artifactId>
          |    <version>${artifact.version}</version>
          |</dependency>""".stripMargin)
    }

    println(artifacts.map(Utils.loadVersions))

    if (gradleFile.exists()) {
      val lines = Source.fromFile(gradleFile).getLines().toList
      val strings = artifactsWithVersions
        .map { case a => s"compile '${a.groupId}:${a.artifactId}:${a.version}'"}
        .toList
      val newContent = new GradleFileAppender().append(lines, strings)
      new SafeFileWriter().write(gradleFile, newContent.mkString("\n") + "\n")
    }
  }
}
