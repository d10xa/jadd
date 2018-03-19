package ru.d10xa.jadd.pipelines

import java.io.File

class MavenPipeline(userDir: File) extends Pipeline {

  lazy val buildFile = new File(userDir, "pom.xml")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {

  }

}

object MavenPipeline {
  def apply(userDir: File): MavenPipeline = new MavenPipeline(userDir)
}
