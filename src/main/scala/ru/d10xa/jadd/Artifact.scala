package ru.d10xa.jadd

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
) {

  def asPath: String = {
    val groupIdPath = groupId.replace('.', '/')
    val art =
      if (needScalaVersionResolving && maybeScalaVersion.isDefined) artifactIdWithScalaVersion(maybeScalaVersion.get)
      else artifactId
    (groupIdPath :: art :: Nil) mkString "/"
  }

  // TODO think about merge needScalaVersionResolving and isScala methods
  def needScalaVersionResolving: Boolean = artifactId.contains("%%")

  def isScala: Boolean = artifactId.endsWith("%%") || maybeScalaVersion.isDefined

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

  def versionsForPrint: String = availableVersions.mkString(", ")

  def canonicalView: String = maybeVersion match {
    case Some(v) => s"$groupId:$artifactId:$v"
    case None => s"$groupId:$artifactId"
  }
}

object Artifact extends StrictLogging {

  implicit class ArtifactImplicits(a: Artifact) {

    def asPaths: Seq[String] = Artifact.artifactAsPaths(a)
    def merge(mavenMetadata: MavenMetadata): Artifact = {
      a.copy(
        availableVersions = mavenMetadata.versions.reverse,
        mavenMetadata = Some(mavenMetadata)
      ).withMetadataUrl(mavenMetadata.url.toString)
    }
    def loadVersions(): Either[ArtifactTrouble, Artifact] = {

      val errOrApi = a.repository match {
        case Some(r) => Right(RepositoryApi.fromString(r))
        case None => Left(RepositoryUndefined(a))
      }

      val errOrMeta =
        errOrApi.flatMap(_.receiveRepositoryMeta(a))

      errOrMeta.foreach(m => logger.info(m.url.getOrElse("Metadata url undefined")))
      val errOrNewArt = errOrMeta.map(a.merge(_))
      errOrNewArt
    }

    def initLatestVersion(versionTools: VersionTools = VersionTools): Artifact =
      a.copy(maybeVersion = versionTools.excludeNonRelease(a.availableVersions).headOption)

  }

  def apply(s: String): Artifact = fromString(s).right.get

  def fromTuple(t: (String, String, String)): Artifact = Artifact(t._1, t._2, Some(t._3))

  def fromTuple(t: (String, String)): Artifact = Artifact(t._1, t._2, None)

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

  def artifactAsPaths(artifact: Artifact): Seq[String] =
    (artifact.needScalaVersionResolving, artifact.maybeScalaVersion) match {
      case (_, Some(scalaVersion)) =>
        Seq(artifact.copy(maybeScalaVersion = Some(scalaVersion)).asPath)
      case (true, None) => Seq(
        artifact.copy(maybeScalaVersion = Some("2.12")).asPath,
        artifact.copy(maybeScalaVersion = Some("2.11")).asPath
      )
      case _ =>
        Seq(artifact.asPath)
    }
}
