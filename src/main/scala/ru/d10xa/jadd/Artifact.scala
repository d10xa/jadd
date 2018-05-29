package ru.d10xa.jadd

import cats.Show
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
    require(artifactId.contains("%%"), "scala version resolving require placeholder %%")
    artifactId.replace("%%", s"_$v")
  }

  def withMetadataUrl(url: String): Artifact = {
    val newMeta: Option[MavenMetadata] =
      mavenMetadata.map(meta => meta.copy(url = Some(url)))
        .orElse(Some(MavenMetadata(url = Some(url))))
    this.copy(mavenMetadata = newMeta)
  }

}

object Artifact {

  implicit val artifactShow: Show[Artifact] = Show[Artifact] { a => s"${a.groupId}:${a.artifactId}" }

  def fromString(artifactRaw: String): Either[ArtifactTrouble, Artifact] = {
    import cats.syntax.either._
    artifactRaw.split(":") match {
      case Array(a, b) =>
        Artifact(
          groupId = a,
          artifactId = b
        ).asRight
      case _ => WrongArtifactRaw.asLeft
    }
  }
}
