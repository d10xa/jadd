package ru.d10xa.jadd.view

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Scope.Test

class SbtArtifactView(
  artifact: Artifact
) extends ArtifactView {
  override def showLines: Seq[String] = {
    val artifactId = artifact.artifactId
    val groupId = artifact.groupId
    val version = artifact.maybeVersion.get // TODO get
    artifact.scope match {
      case Some(Test) =>
        s"""libraryDependencies += "$groupId" % "$artifactId" % "$version" % Test""" :: Nil
      case _ =>
        s"""libraryDependencies += "$groupId" % "$artifactId" % "$version"""" :: Nil
    }
  }
}
