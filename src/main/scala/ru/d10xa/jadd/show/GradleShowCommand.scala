package ru.d10xa.jadd.show

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.ArtifactProvider.GradleBuildDescription

class GradleShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): Seq[Artifact] =
    GradleBuildDescription(buildFileSource).artifacts
}
