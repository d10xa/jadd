package ru.d10xa.jadd.pipelines

import java.io.File

class SbtPipeline(userDir: File) extends Pipeline {

  lazy val buildFile = new File(userDir, "build.sbt")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {

  }

}

object SbtPipeline {
  def apply(userDir: File): SbtPipeline = new SbtPipeline(userDir)
}
