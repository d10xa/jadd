package ru.d10xa.jadd

import java.io.File
import java.net.URI

import scala.util.Try
import scala.xml.XML

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
    versions.filter { version => !exclude.exists(version.toLowerCase.contains(_)) }
  }

  def collectMetadataUris(
    artifact: Artifact,
    remoteRepo: String,
    localRepo: => File
  ): Stream[MetadataUri] = {

    val metadataUris: Stream[MetadataUri] =
      if (artifact.needScalaVersionResolving) {
        Seq("2.12", "2.11")
          .toStream
          .map(v => ScalaMetadataUri(artifact.copy(maybeScalaVersion = Some(v)), v, remoteRepo))
      } else {
        Seq(
          SimpleMetadataUri(remoteRepo, artifact),
          LocalMetadataUri(localRepo, artifact)
        ).toStream
      }
    metadataUris
  }

  def loadVersions(artifact: Artifact): (Artifact, Seq[String]) = {
    // TODO get repository path from ~/.m2/settings.xml or use default
    val uris: Stream[MetadataUri] = collectMetadataUris(
      artifact,
      "https://jcenter.bintray.com",
      new File(s"${System.getProperty("user.home")}/.m2/repository")
    )

    val opt: Option[(Artifact, Seq[String])] = uris.map { uri =>
      Try {
        val elem = XML.load(uri.uri.toURL)
        val versions = MavenMetadataVersionsRawReader.versionsDesc(elem)
        println(uri.uri)
        versions
      }
        .toOption
        .map { versions => uri.artifact -> versions }
    }.collectFirst {
      case Some(t) => t
    }
    opt.get // TODO refactoring
  }

  def loadLatestVersion(artifact: Artifact): Artifact = {
    val (newArtifact, versions) = Utils.loadVersions(artifact)
    newArtifact.copy(maybeVersion = Utils.excludeNonRelease(versions).headOption)
  }

  def mkArtifact(raw: String): Artifact = {
    val Array(a, b) = raw.split(':')
    Artifact(a, b, maybeVersion = None, maybeScalaVersion = None)
  }

  def unshortAll(rawDependencies: List[String], unshort: String => Option[String]): List[Artifact] =
    rawDependencies
      .map(raw => unshort(raw).getOrElse(raw))
      .map(mkArtifact)

  def shortcutLineToArtifact(line: String): (String, Artifact) = {
    line.split(',') match {
      case Array(short, full) =>
        val Array(a, b) = full.split(":")
        short -> Artifact(a, b, maybeVersion = None, maybeScalaVersion = None)
      case wrongArray => throw new IllegalArgumentException(s"wrong array $wrongArray")
    }
  }
}
