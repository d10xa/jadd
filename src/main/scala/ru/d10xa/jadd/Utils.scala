package ru.d10xa.jadd

import java.io.File
import java.net.URI

import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.ArtifactNotFoundByAlias
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.ArtifactTrouble

import scala.io.BufferedSource
import scala.io.Source
import scala.xml.XML
import cats.syntax.either._
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.LoadVersionsTrouble

import scala.util.Try

object Utils {

  sealed trait MetadataUri {
    def uri: URI
    def artifact: Artifact
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
    repo: File,
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
    localRepo: => File
  ): Stream[MetadataUri] = {

    val repo = artifact.repositoryPath.getOrElse(defaultRemoteRepo)

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

  def loadVersions(artifact: Artifact): Either[ArtifactTrouble, Artifact] = {
    // TODO get repository path from ~/.m2/settings.xml or use default
    val uris: Stream[MetadataUri] = collectMetadataUris(
      artifact,
      "https://jcenter.bintray.com",
      new File(s"${System.getProperty("user.home")}/.m2/repository")
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
        .leftMap(_ => LoadVersionsTrouble)
        .map { versions => uri.artifact.copy(availableVersions = versions) }
    }
    opt.collectFirst {
      case r @ Right(_) => r
    }.getOrElse(LoadVersionsTrouble.asLeft)
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
        case Left(ArtifactNotFoundByAlias) =>
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
