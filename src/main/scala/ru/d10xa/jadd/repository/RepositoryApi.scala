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
  def makeFullMetadataUrl(path: String): String = {
    s"$absoluteRepositoryPath/$path/$mavenMetadataXmlName"
  }
  def receiveRepositoryMetaWithUrl(artifact: Artifact, url: String): Either[MetadataLoadTrouble, MavenMetadata]
  def receiveRepositoryMeta(artifact: Artifact): Either[MetadataLoadTrouble, MavenMetadata] = {
    val paths = artifact.asPaths

    val seq: Seq[Either[MetadataLoadTrouble, MavenMetadata]] =
      paths.toStream.map { p =>
        receiveRepositoryMetaWithUrl(artifact, p)
      }
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

  override def absoluteRepositoryPath: String = RepositoryApi.rtrimSlash(repository)

  override def receiveRepositoryMetaWithUrl(
    artifact: Artifact,
    path: String
  ): Either[MetadataLoadTrouble, MavenMetadata] = {
    try {
      val url = makeFullMetadataUrl(path)
      val rootElem = XML.load(new URL(url))
      val meta =
        MavenMetadata
          .readFromXml(MavenMetadata(url = Some(url)), rootElem)
          .copy(url = Some(url), repository = Some(repository))
      Right(meta)
    } catch {
      case NonFatal(e) => Left(MetadataLoadTrouble(artifact, e.getMessage))
    }
  }
}

class MavenLocalMetadataRepositoryApi(val repository: String)
  extends RepositoryApi[MavenMetadata]
  with MavenMetadataBase {

  override def absoluteRepositoryPath: String =
    new File(repository).getAbsolutePath

  override def mavenMetadataXmlName: String = "maven-metadata-local.xml"

  override def receiveRepositoryMetaWithUrl(
    artifact: Artifact,
    path: String
  ): Either[MetadataLoadTrouble, MavenMetadata] = {
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
}
