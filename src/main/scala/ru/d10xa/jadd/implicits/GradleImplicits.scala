package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.inserts.GradleArtifactMatcher
import ru.d10xa.jadd.view.ArtifactView
import ru.d10xa.jadd.view.ArtifactView.Match

trait GradleImplicits {

  implicit val sbtArtifactView: ArtifactView[Artifact] = new ArtifactView[Artifact] {
    override def showLines(artifact: Artifact): Seq[String] = {

      def artifactToString(a: Artifact): String =
        if (a.scope.contains(Test)) s"""testCompile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""
        else s"""compile "${a.groupId}:${a.artifactId}:${a.maybeVersion.get}""""

      artifactToString(artifact.inlineScalaVersion) :: Nil
    }

    override def find(artifact: Artifact, source: String): Seq[Match] =
      new GradleArtifactMatcher(source).find(artifact)
  }

}
