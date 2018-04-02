package ru.d10xa.jadd

sealed trait Artifact {
  def needScalaVersionResolving: Boolean
  def asPath: String
  def artifactId: String
  def groupId: String
  def artifactIdWithScalaVersion(v: String): String = {
    require(artifactId.contains("%%"), "scala version resolving require placeholder %%")
    artifactId.replace("%%", s"_$v")
  }
}

object Artifact {
  def noVersion(groupId: String, artifactId: String): Artifact =
    new ArtifactWithoutVersion(groupId, artifactId)
  def withVersion(groupId: String, artifactId: String, version: String): Artifact =
    new ArtifactWithVersion(groupId, artifactId, version)
}

final class ArtifactWithoutVersion(val groupId: String, val artifactId: String) extends Artifact {

  def copy(groupId: String = groupId, artifactId: String = artifactId): ArtifactWithoutVersion =
    new ArtifactWithoutVersion(groupId, artifactId)

  def withVersion(version: String): ArtifactWithVersion =
    new ArtifactWithVersion(groupId, artifactId, version)

  override def needScalaVersionResolving: Boolean = artifactId.contains("%%")

  override def asPath: String = // groupId.replace('.', '/') + "/" + artifactId
    (groupId.replace('.', '/') :: artifactId :: Nil)
      .mkString("/")

  def resolveScalaVersion(v: String): ArtifactWithoutVersion =
    copy(artifactId = artifactIdWithScalaVersion(v))
}

final class ArtifactWithVersion(val groupId: String, val artifactId: String, val version: String) extends Artifact {

  def copy(groupId: String = groupId, artifactId: String = artifactId, version: String = version): ArtifactWithVersion =
    new ArtifactWithVersion(groupId, artifactId, version)

  override def needScalaVersionResolving: Boolean = false

  override def asPath: String =
    (groupId.replace('.', '/') :: artifactId :: version :: Nil)
      .mkString("/")

}
