package ru.d10xa.jadd

import cats.data.EitherNel
import cats.data.NonEmptyList
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.repository.RepositoryApi
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.RepositoryUndefined
import ru.d10xa.jadd.troubles.WrongArtifactRaw
import ru.d10xa.jadd.versions.VersionTools

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

  def merge(mavenMetadata: MavenMetadata): Artifact =
    copy(
      availableVersions = mavenMetadata.versions.reverse,
      mavenMetadata = Some(mavenMetadata),
      maybeScalaVersion =
        maybeScalaVersion.orElse(mavenMetadata.maybeScalaVersion)
    ).withMetadataUrl(mavenMetadata.url.toString)

  def loadVersions(): EitherNel[ArtifactTrouble, Artifact] = {
    val errOrApi: Either[RepositoryUndefined, RepositoryApi[MavenMetadata]] =
      repository match {
        case Some(r) => Right(RepositoryApi.fromString(r))
        case None => Left(RepositoryUndefined(this))
      }
    val errOrMeta: Either[NonEmptyList[ArtifactTrouble], MavenMetadata] =
      errOrApi.left.map(NonEmptyList.one).flatMap(_.receiveRepositoryMeta(this))

    val errOrNewArt = errOrMeta.map(merge)
    errOrNewArt
  }

  def initLatestVersion(versionTools: VersionTools = VersionTools): Artifact =
    copy(
      maybeVersion =
        versionTools.excludeNonRelease(availableVersions).headOption)

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
