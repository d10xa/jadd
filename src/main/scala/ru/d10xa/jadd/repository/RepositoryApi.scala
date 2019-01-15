package ru.d10xa.jadd.repository

import java.io.File
import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles.MetadataLoadTrouble

import scala.util.control.NonFatal
import scala.xml.XML

sealed trait RepositoryApi[+T <: ArtifactRepositoryMeta] {
  def repository: String
  def receiveRepositoryMeta(artifact: Artifact): Either[MetadataLoadTrouble, T]
}

object RepositoryApi {

  def fromString(repository: String): RepositoryApi[MavenMetadata] =
    if (repository.startsWith("http")) {
      new MavenRemoteMetadataRepositoryApi(repository)
    } else {
      new MavenLocalMetadataRepositoryApi(repository)
    }

  def rtrimSlash(string: String): String =
    string.replaceAll("/$", "")

}

trait MavenMetadataBase extends LazyLogging {
  def mavenMetadataXmlName: String
  def repository: String
  def absoluteRepositoryPath: String
  def makeFullMetadataUrl(path: String): String =
    s"$absoluteRepositoryPath/$path/$mavenMetadataXmlName"
  def receiveRepositoryMetaWithArtifactPath(
    artifact: Artifact,
    artifactPath: String): Either[MetadataLoadTrouble, MavenMetadata]
  def receiveRepositoryMeta(
    artifact: Artifact): Either[MetadataLoadTrouble, MavenMetadata] = {

    val seq = if (artifact.isScala && artifact.maybeScalaVersion.isEmpty) {
      Seq("2.12", "2.11").toStream
        .map(scalaVersion =>
          artifact.copy(maybeScalaVersion = Some(scalaVersion)))
        .map(a => receiveRepositoryMetaWithArtifactPath(a, a.asPath))
    } else {
      Seq(receiveRepositoryMetaWithArtifactPath(artifact, artifact.asPath))
    }

    // TODO refactoring
    seq
      .collectFirst { case Right(value) => value }
      .map(Right(_))
      .getOrElse(Left(MetadataLoadTrouble(artifact, seq.head.left.get.cause)))
  }
}

class MavenRemoteMetadataRepositoryApi(val repository: String)
    extends RepositoryApi[MavenMetadata]
    with MavenMetadataBase {

  override def mavenMetadataXmlName: String = "maven-metadata.xml"

  override def absoluteRepositoryPath: String =
    RepositoryApi.rtrimSlash(repository)

  override def receiveRepositoryMetaWithArtifactPath(
    artifact: Artifact,
    path: String
  ): Either[MetadataLoadTrouble, MavenMetadata] =
    try {
      val url = makeFullMetadataUrl(path)
      val rootElem = XML.load(new URL(url))
      val meta =
        MavenMetadata
          .readFromXml(MavenMetadata(url = Some(url)), rootElem)
          .copy(
            url = Some(url),
            repository = Some(repository),
            maybeScalaVersion = artifact.maybeScalaVersion)
      Right(meta)
    } catch {
      case NonFatal(e) => Left(MetadataLoadTrouble(artifact, e.getMessage))
    }
}

class MavenLocalMetadataRepositoryApi(val repository: String)
    extends RepositoryApi[MavenMetadata]
    with MavenMetadataBase {

  override def absoluteRepositoryPath: String =
    new File(repository).getAbsolutePath

  override def mavenMetadataXmlName: String = "maven-metadata-local.xml"

  override def receiveRepositoryMetaWithArtifactPath(
    artifact: Artifact,
    path: String
  ): Either[MetadataLoadTrouble, MavenMetadata] =
    try {
      val url = makeFullMetadataUrl(path)
      val rootElem = XML.loadFile(new File(url))
      val meta =
        MavenMetadata
          .readFromXml(MavenMetadata(url = Some(url)), rootElem)
          .copy(url = Some(url), repository = Some(repository))
      Right(meta)
    } catch {
      case NonFatal(e) => Left(MetadataLoadTrouble(artifact, e.getMessage))
    }
}
