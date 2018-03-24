package ru.d10xa.jadd.pipelines

import java.io.File

import ru.d10xa.jadd.Ctx

class MavenPipeline(ctx: Ctx) extends Pipeline {

  lazy val buildFile = new File(ctx.config.projectDir, "pom.xml")

  override def applicable: Boolean = buildFile.exists()

  override def run(): Unit = {

  }

}

object MavenPipeline {
  def apply(ctx: Ctx): Pipeline = new MavenPipeline(ctx)
}
