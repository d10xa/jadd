package ru.d10xa.jadd

import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.WrongArtifactRaw

final case class Artifact(
  groupId: String,
  artifactId: String,
  maybeVersion: Option[String] = None,
  shortcut: Option[String] = None,
  scope: Option[Scope] = None,
  repository: Option[String] = None,
  mavenMetadata: Option[MavenMetadata] = None,
  maybeScalaVersion: Option[String] = None,
  availableVersions: Seq[String] = Seq.empty,
  explicitScalaVersion: Boolean = false,
  inSequence: Boolean = false // required for ArtifactView
) {

  def asPath: String = {
    val groupIdPath = groupId.replace('.', '/')
    val art =
      if (needScalaVersionResolving && maybeScalaVersion.isDefined) artifactIdWithScalaVersion(maybeScalaVersion.get)
      else artifactId
    val l: Seq[String] = groupIdPath :: art :: Nil
    maybeVersion
      .map(l :+ _)
      .getOrElse(l)
      .mkString("/")
  }

  // TODO think about merge needScalaVersionResolving and isScala methods
  def needScalaVersionResolving: Boolean = artifactId.contains("%%")

  def isScala: Boolean = artifactId.endsWith("%%")

  def artifactIdWithoutScalaVersion: String = {
    if (isScala) artifactId.substring(0, artifactId.length - 2)
    else artifactId
  }

  def artifactIdWithScalaVersion(v: String): String = {
    require(artifactId.endsWith("%%"), "scala version resolving require placeholder %%")
    artifactId.replace("%%", s"_$v")
  }

  def withMetadataUrl(url: String): Artifact = {
    val newMeta: Option[MavenMetadata] =
      mavenMetadata.map(meta => meta.copy(url = Some(url)))
        .orElse(Some(MavenMetadata(url = Some(url))))
    this.copy(mavenMetadata = newMeta)
  }

  def inlineScalaVersion: Artifact = Artifact.inlineScalaVersion(this)

}

object Artifact {

  def apply(s: String): Artifact = fromString(s).right.get

  def fromString(artifactRaw: String): Either[ArtifactTrouble, Artifact] = {
    artifactRaw.split(":") match {
      case Array(g, a) =>
        Right(Artifact(
          groupId = g,
          artifactId = a
        ))
      case Array(g, a, v) =>
        Right(Artifact(
          groupId = g,
          artifactId = a,
          maybeVersion = Some(v)
        ))
      case _ => Left(WrongArtifactRaw)
    }
  }

  def inlineScalaVersion(artifact: Artifact): Artifact = {
    artifact.maybeScalaVersion.map { v =>
      artifact.copy(artifactId = artifact.artifactIdWithScalaVersion(v))
    }.getOrElse(artifact)
  }
}
