package ru.d10xa.jadd.show

import cats.data.Chain
import ru.d10xa.jadd.core.ArtifactProvider.GradleBuildDescription
import ru.d10xa.jadd.core.Artifact

class GradleShowCommand(buildFileSource: String) {
  def show(): Chain[Artifact] =
    GradleBuildDescription(buildFileSource).artifacts
}
