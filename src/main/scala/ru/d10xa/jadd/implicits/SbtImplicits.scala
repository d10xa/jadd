package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.view.ArtifactView

trait SbtImplicits {

  implicit val sbtArtifactView: ArtifactView[Artifact] = (artifact: Artifact) => {
    val groupId = artifact.groupId
    val version = artifact.maybeVersion.get // TODO get

    val groupAndArtifact = (artifact.explicitScalaVersion, artifact.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) if artifact.isScala =>
        s""""$groupId" % "${artifact.artifactIdWithScalaVersion(scalaVersion)}""""
      case (false, Some(_)) =>
        s""""$groupId" %% "${artifact.artifactIdWithoutScalaVersion}""""
      case (_, None) =>
        s""""$groupId" % "${artifact.artifactId}""""
    }

    val prefix = if (artifact.inSequence) "" else "libraryDependencies += "

    artifact.scope match {
      case Some(Test) =>
        s"""$prefix$groupAndArtifact % "$version" % Test""" :: Nil
      case _ =>
        s"""$prefix$groupAndArtifact % "$version"""" :: Nil
    }
  }

}
