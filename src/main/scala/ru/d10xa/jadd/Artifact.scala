package ru.d10xa.jadd

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.WrongArtifactRaw
import ru.d10xa.jadd.versions.ScalaVersions
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
  doubleQuotes: Boolean = true, // required for gradle update
  configuration: Option[String] = None, // required for gradle update
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

  def merge(mavenMetadata: MavenMetadata): Artifact = {
    val updated = this
      .copy(
        availableVersions = mavenMetadata.versions.reverse,
        mavenMetadata = Some(mavenMetadata),
        maybeScalaVersion =
          this.maybeScalaVersion.orElse(mavenMetadata.maybeScalaVersion)
      )
    mavenMetadata.url.fold(updated)(updated.withMetadataUrl)
  }

  def withMetadataUrl(url: String): Artifact = {
    val newMeta: Option[MavenMetadata] =
      this.mavenMetadata
        .map(meta => meta.copy(url = Some(url)))
        .orElse(Some(MavenMetadata(url = Some(url))))
    this.copy(mavenMetadata = newMeta)
  }

  def inlineScalaVersion: Artifact = Artifact.inlineScalaVersion(this)

  def versionsForPrint: String = availableVersions.mkString(", ")

  def initLatestVersion(
    versionFilter: VersionFilter = VersionFilter): Artifact =
    copy(
      maybeVersion =
        versionFilter.excludeNonRelease(availableVersions).headOption)

}

object Artifact {

  def fromTuple3(t: (String, String, String)): Artifact =
    Artifact(t._1, t._2, Some(t._3))

  def fromTuple2(t: (String, String)): Artifact =
    Artifact(t._1, t._2, None)

  /**
    * Example: Split artifact id cats-core_2.12 to tuple (cats-core%%, Some(2.12))
    */
  def scalaVersionAsPlaceholders(
    artifactId: String): (String, Option[String]) = {

    val foundScalaVersion: Option[String] =
      ScalaVersions.supportedMinorVersions.find(v =>
        artifactId.contains(s"_$v"))
    foundScalaVersion match {
      case Some(v) =>
        (artifactId.dropRight("_".length + v.length) + "%%", Some(v))
      case None => (artifactId, None)
    }
  }

  def fromString(artifactRaw: String): Either[ArtifactTrouble, Artifact] =
    artifactRaw.split(":") match {
      case Array(g, a) =>
        val (artifactId, maybeScalaVersion) = scalaVersionAsPlaceholders(a)
        Right(
          Artifact(
            groupId = g,
            artifactId = artifactId,
            maybeScalaVersion = maybeScalaVersion
          ))
      case Array(g, a, v) =>
        val (artifactId, maybeScalaVersion) = scalaVersionAsPlaceholders(a)
        Right(
          Artifact(
            groupId = g,
            artifactId = artifactId,
            maybeVersion = Some(v),
            maybeScalaVersion = maybeScalaVersion
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
