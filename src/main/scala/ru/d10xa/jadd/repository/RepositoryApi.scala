package ru.d10xa.jadd.repository

import java.net.URL

import better.files._
import cats.data.EitherNel
import cats.data.NonEmptyList
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import coursier.core.Version
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles.MetadataLoadTrouble
import ru.d10xa.jadd.versions.ScalaVersions

import scala.util.control.NonFatal
import scala.xml.Elem
import scala.xml.XML

trait RepositoryApi {
  def repository: String
  def receiveRepositoryMetaWithMaxVersion(
    artifact: Artifact): EitherNel[MetadataLoadTrouble, MavenMetadata]
}

object RepositoryApi {

  val metadataWithMaxVersion: Seq[MavenMetadata] => Option[MavenMetadata] =
    metas =>
      metas.filter(_.versions.nonEmpty) match {
        case xs if xs.isEmpty => None
        case xs => xs.maxBy(_.versions.map(Version(_)).max).some
    }

  def fromString(repository: String): RepositoryApi =
    if (repository.startsWith("http")) {
      new MavenRemoteMetadataRepositoryApi(repository)
    } else {
      new MavenLocalMetadataRepositoryApi(repository)
    }

  def rtrimSlash(string: String): String =
    string.replaceAll("/$", "")

}

trait MavenMetadataBase extends RepositoryApi with LazyLogging {
  def mavenMetadataXmlName: String
  def repository: String
  def absoluteRepositoryPath: String
  def makeFullMetadataUrl(path: String): String =
    s"$absoluteRepositoryPath/$path/$mavenMetadataXmlName"
  def receiveRepositoryMetaWithArtifactPath(
    artifact: Artifact,
    artifactPath: String): Either[MetadataLoadTrouble, MavenMetadata]
  def receiveRepositoryMetaWithMaxVersion(
    artifact: Artifact): EitherNel[MetadataLoadTrouble, MavenMetadata] = {

    val metas: NonEmptyList[Either[MetadataLoadTrouble, MavenMetadata]] =
      if (artifact.isScala && artifact.maybeScalaVersion.isEmpty) {
        ScalaVersions.supportedMinorVersions
          .map(scalaVersion =>
            artifact.copy(maybeScalaVersion = Some(scalaVersion)))
          .map(a => receiveRepositoryMetaWithArtifactPath(a, a.asPath))
      } else {
        NonEmptyList.one(
          receiveRepositoryMetaWithArtifactPath(artifact, artifact.asPath))
      }

    val (troubles, maybeArtifactList) = metas.toList.separate

    val result: EitherNel[MetadataLoadTrouble, MavenMetadata] =
      if (maybeArtifactList.nonEmpty) {
        RepositoryApi.metadataWithMaxVersion(maybeArtifactList) match {
          case Some(meta) => meta.asRight
          case None =>
            MetadataLoadTrouble(artifact, "Versions not found").asLeft.toEitherNel
        }
      } else troubles.toNel.get.asLeft

    result.foreach((meta: MavenMetadata) =>
      logger.debug(s"Metadata with max version: $meta"))

    result
  }
}

final class MavenRemoteMetadataRepositoryApi(val repository: String)
    extends RepositoryApi
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

final class MavenLocalMetadataRepositoryApi(val repository: String)
    extends RepositoryApi
    with MavenMetadataBase {

  override def absoluteRepositoryPath: String =
    File(repository).canonicalPath

  override def mavenMetadataXmlName: String = "maven-metadata-local.xml"

  override def receiveRepositoryMetaWithArtifactPath(
    artifact: Artifact,
    path: String
  ): Either[MetadataLoadTrouble, MavenMetadata] =
    try {
      val url = makeFullMetadataUrl(path)
      val rootElem: Elem =
        File(url).bufferedReader.map(XML.load).get()
      val meta =
        MavenMetadata
          .readFromXml(MavenMetadata(url = Some(url)), rootElem)
          .copy(url = Some(url), repository = Some(repository))
      Right(meta)
    } catch {
      case NonFatal(e) => Left(MetadataLoadTrouble(artifact, e.getMessage))
    }
}
