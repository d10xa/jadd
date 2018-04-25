package ru.d10xa.jadd

import java.io.File
import java.io.IOException
import java.net.URI

import cats.syntax.either._
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.troubles.ArtifactNotFoundByAlias
import ru.d10xa.jadd.troubles.ArtifactTrouble
import ru.d10xa.jadd.troubles.LoadVersionsTrouble

import scala.io.BufferedSource
import scala.io.Source
import scala.util.Try
import scala.xml.XML

object Utils {

  sealed trait MetadataUri {
    def uri: URI
    def artifact: Artifact
    def repo: String
  }
  final case class ScalaMetadataUri(
    artifact: Artifact,
    scalaVersion: String,
    repo: String
  ) extends MetadataUri {
    override def uri: URI = {
      val a = artifact.copy(maybeScalaVersion = Some(scalaVersion))
      new URI(s"""$repo/${a.asPath}/maven-metadata.xml""")
    }
  }
  final case class SimpleMetadataUri(
    repo: String,
    artifact: Artifact
  ) extends MetadataUri {
    override def uri: URI = {
      new URI(s"$repo/${artifact.asPath}/maven-metadata.xml")
    }
  }

  final case class LocalMetadataUri(
    repo: String,
    artifact: Artifact
  ) extends MetadataUri {
    override def uri: URI = {
      new File(repo, s"${artifact.asPath}/maven-metadata-local.xml").toURI
    }
  }

  def excludeNonRelease(versions: Seq[String]): Seq[String] = {
    val exclude = Seq("rc", "alpha", "beta", "m")
    val filteredVersions =
      versions.filter { version => !exclude.exists(version.toLowerCase.contains(_)) }
    if (filteredVersions.isEmpty) versions else filteredVersions
  }

  def collectMetadataUris(
    artifact: Artifact,
    defaultRemoteRepo: String,
    localRepo: => String
  ): Stream[MetadataUri] = {

    val repo = artifact.repository.getOrElse(defaultRemoteRepo)

    val metadataUris: Stream[MetadataUri] =
      if (artifact.needScalaVersionResolving) {
        Seq("2.12", "2.11")
          .toStream
          .map(v => ScalaMetadataUri(artifact.copy(maybeScalaVersion = Some(v)), v, repo))
      } else {
        Seq(
          SimpleMetadataUri(repo, artifact),
          LocalMetadataUri(localRepo, artifact)
        ).toStream
      }
    metadataUris
  }

  def loadVersions(artifact: Artifact, metadataUri: MetadataUri): Either[ArtifactTrouble, Artifact] = {
    import cats.implicits._
    Either.catchOnly[IOException]{
      val elem = XML.load(metadataUri.uri.toURL)
      MavenMetadataVersionsRawReader.versionsDesc(elem)
    }.bimap(e =>
      LoadVersionsTrouble(metadataUri, e.toString),
      vs => artifact.copy(
        availableVersions = vs,
        repository = Some(metadataUri.repo),
        metadataUrl = Some(metadataUri.uri.toString)
      )
    )
  }

  def loadVersions(artifact: Artifact): Either[ArtifactTrouble, Artifact] = {
    // TODO get repository path from ~/.m2/settings.xml or use default
    val uris: Stream[MetadataUri] = collectMetadataUris(
      artifact,
      "https://jcenter.bintray.com",
      s"${System.getProperty("user.home")}/.m2/repository"
    )

    def readVersions(uri: MetadataUri): Seq[String] = {
      val elem = XML.load(uri.uri.toURL)
      val versions = MavenMetadataVersionsRawReader.versionsDesc(elem)
      println(uri.uri)
      versions
    }

    val opt: Seq[Either[ArtifactTrouble, Artifact]] = uris.map { uri =>
      Try(readVersions(uri))
        .toEither
        .leftMap(e => LoadVersionsTrouble(uri, e.toString))
        .map { versions => uri.artifact.copy(availableVersions = versions) }
    }
    opt.head // TODO head may throw exception
  }

  def loadLatestVersion(artifact: Artifact): Either[ArtifactTrouble, Artifact] = {
    def initLatestVersion(a: Artifact): Artifact =
      a.copy(maybeVersion = Utils.excludeNonRelease(a.availableVersions).headOption)
    Utils
      .loadVersions(artifact)
      .map(initLatestVersion)
  }

  def unshortAll(rawDependencies: List[String], artifactInfoFinder: ArtifactInfoFinder): List[Artifact] =
    rawDependencies
      .flatMap(raw => artifactInfoFinder.artifactFromString(raw) match {
        case Right(artifact) => artifact :: Nil
        case Left(_: ArtifactNotFoundByAlias) =>
          println(s"$raw - artifact not found by shortcut")
          Nil
        case Left(trouble) =>
          println(s"some error occurred $trouble")
          Nil
      })

  def sourceFromSpringUri(string: String): BufferedSource =
    if (string.startsWith("classpath:")) Source.fromResource(string.drop(10))
    else Source.fromURL(string)

}
