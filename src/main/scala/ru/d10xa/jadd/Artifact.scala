package ru.d10xa.jadd

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.WrongArtifactRaw
import ru.d10xa.jadd.versions.VersionFilter

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
) extends StrictLogging {

  def asPath: String = {
    val groupIdPath = groupId.replace('.', '/')
    (needScalaVersionResolving, maybeScalaVersion) match {
      case (false, None) =>
        s"$groupIdPath/$artifactId"
      case (true, Some(scalaVersion)) =>
        s"$groupIdPath/${artifactIdWithScalaVersion(scalaVersion)}"
      case _ =>
        throw new IllegalStateException(
          s"artifact $artifactId cannot be represented as path")
    }
  }

  // TODO think about merge needScalaVersionResolving and isScala methods
  def needScalaVersionResolving: Boolean = artifactId.contains("%%")

  def isScala: Boolean =
    artifactId.endsWith("%%") || maybeScalaVersion.isDefined

  def artifactIdWithoutScalaVersion: String =
    if (isScala) artifactId.substring(0, artifactId.length - 2)
    else artifactId

  def artifactIdWithScalaVersion(v: String): String = {
    require(
      artifactId.endsWith("%%"),
      "scala version resolving require placeholder %%")
    artifactId.replace("%%", s"_$v")
  }

  def merge(mavenMetadata: MavenMetadata): Artifact =
    this
      .copy(
        availableVersions = mavenMetadata.versions.reverse,
        mavenMetadata = Some(mavenMetadata),
        maybeScalaVersion =
          this.maybeScalaVersion.orElse(mavenMetadata.maybeScalaVersion)
      )
      .withMetadataUrl(mavenMetadata.url.toString)

  def withMetadataUrl(url: String): Artifact = {
    val newMeta: Option[MavenMetadata] =
      mavenMetadata
        .map(meta => meta.copy(url = Some(url)))
        .orElse(Some(MavenMetadata(url = Some(url))))
    this.copy(mavenMetadata = newMeta)
  }

  def inlineScalaVersion: Artifact = Artifact.inlineScalaVersion(this)

  def versionsForPrint: String = availableVersions.mkString(", ")

  def canonicalView: String = maybeVersion match {
    case Some(v) => s"$groupId:$artifactId:$v"
    case None => s"$groupId:$artifactId"
  }

  def initLatestVersion(
    versionFilter: VersionFilter = VersionFilter): Artifact =
    copy(
      maybeVersion =
        versionFilter.excludeNonRelease(availableVersions).headOption)

}

object Artifact {

  def apply(s: String): Artifact = fromString(s).right.get

  def fromTuple3(t: (String, String, String)): Artifact =
    Artifact(t._1, t._2, Some(t._3))

  def fromTuple2(t: (String, String)): Artifact =
    Artifact(t._1, t._2, None)

  def fromString(artifactRaw: String): Either[ArtifactTrouble, Artifact] =
    artifactRaw.split(":") match {
      case Array(g, a) =>
        Right(
          Artifact(
            groupId = g,
            artifactId = a
          ))
      case Array(g, a, v) =>
        Right(
          Artifact(
            groupId = g,
            artifactId = a,
            maybeVersion = Some(v)
          ))
      case _ => Left(WrongArtifactRaw)
    }

  def inlineScalaVersion(artifact: Artifact): Artifact =
    artifact.maybeScalaVersion
      .map { v =>
        artifact.copy(artifactId = artifact.artifactIdWithScalaVersion(v))
      }
      .getOrElse(artifact)

}
