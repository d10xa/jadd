package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.view.ArtifactView

trait GradleImplicits {

  implicit val gradleArtifactView: ArtifactView[Artifact] = (artifact: Artifact) => {

    def artifactToString(a: Artifact): String =
      if (a.scope.contains(Test))
        s"""testCompile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""
      else
        s"""compile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""

    artifactToString(artifact.inlineScalaVersion) :: Nil
  }

}
