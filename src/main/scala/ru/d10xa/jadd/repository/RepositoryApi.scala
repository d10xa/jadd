package ru.d10xa.jadd.repository

import java.net.URL

import better.files._
import cats.data.EitherNel
import cats.data.NonEmptyList
import com.typesafe.scalalogging.LazyLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles.MetadataLoadTrouble

import scala.collection.immutable.Stream
import scala.collection.immutable.Stream.cons
import scala.util.control.NonFatal
import scala.xml.Elem
import scala.xml.XML

sealed trait RepositoryApi {
  def repository: String
  def receiveRepositoryMeta(
    artifact: Artifact): EitherNel[MetadataLoadTrouble, MavenMetadata]
}

object RepositoryApi {

  def fromString(repository: String): RepositoryApi =
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
    artifact: Artifact): EitherNel[MetadataLoadTrouble, MavenMetadata] = {

    implicit class StreamImplicits[A](s: Stream[A]) {
      def takeUntil(p: A => Boolean): Stream[A] =
        if (s.isEmpty) Stream.empty
        else if (p(s.head)) cons(s.head, s.tail.takeUntil(p))
        else Stream(s.head)
    }

    import cats.implicits._

    val metas: NonEmptyList[Either[MetadataLoadTrouble, MavenMetadata]] =
      if (artifact.isScala && artifact.maybeScalaVersion.isEmpty) {

        List("2.12", "2.11").toStream
          .map(scalaVersion =>
            artifact.copy(maybeScalaVersion = Some(scalaVersion)))
          .map(a => receiveRepositoryMetaWithArtifactPath(a, a.asPath))
          .takeUntil(_.isLeft) // take all Left and single Right
          .toList
          .toNel
          .get // get is safe, because of explicit initial List definition
      } else {
        NonEmptyList.one(
          receiveRepositoryMetaWithArtifactPath(artifact, artifact.asPath))
      }

    val (troubles, maybeArtifactList) = metas.toList.separate

    if (maybeArtifactList.nonEmpty) maybeArtifactList.head.asRight
    else troubles.toNel.get.asLeft
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
