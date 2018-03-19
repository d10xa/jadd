package ru.d10xa.jadd.pipelines

import java.io.File

class GradlePipeline(userDir: File) extends Pipeline {

  lazy val buildFile = new File(userDir, "build.gradle")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {

  }
}

object GradlePipeline {
  def apply(userDir: File): GradlePipeline = new GradlePipeline(userDir)
}
