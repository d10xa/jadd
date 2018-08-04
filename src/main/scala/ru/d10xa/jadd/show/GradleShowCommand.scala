package ru.d10xa.jadd.show

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.ArtifactProvider.GradleBuildDescription

class GradleShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): String =
    GradleBuildDescription(buildFileSource)
      .artifacts
      .map(_.canonicalView)
      .mkString("\n")
}
