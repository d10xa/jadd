package ru.d10xa.jadd

sealed trait Artifact

object Artifact {
  def noVersion(groupId: String, artifactId: String): Artifact =
    ArtifactWithoutVersion(groupId, artifactId)
  def withVersion(groupId: String, artifactId: String, version: String): Artifact =
    ArtifactWithVersion(groupId, artifactId, version)
}

final case class ArtifactWithoutVersion(groupId: String, artifactId: String) extends Artifact {
  def withVersion(version: String): ArtifactWithVersion =
    ArtifactWithVersion(groupId, artifactId, version)
}

final case class ArtifactWithVersion(groupId: String, artifactId: String, version: String) extends Artifact
