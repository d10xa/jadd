package ru.d10xa.jadd

import java.io.File
import java.net.URI

import scala.util.Try
import scala.xml.XML

object Utils {

  sealed trait MetadataUri {
    def uri: URI
  }
  final case class ScalaMetadataUri(
    artifact: ArtifactWithoutVersion,
    scalaVersion: String,
    repo: String
  ) extends MetadataUri {
    override def uri: URI = {
      val a = artifact.resolveScalaVersion(scalaVersion)
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
    artifact: ArtifactWithoutVersion,
    remoteRepo: String,
    localRepo: => File
  ): Stream[URI] = {

    val metadataUris: Stream[MetadataUri] =
      if (artifact.needScalaVersionResolving) {
        Seq("2.12", "2.11")
          .toStream
          .map(ScalaMetadataUri(artifact, _, remoteRepo))
      } else {
        Seq(
          SimpleMetadataUri(remoteRepo, artifact),
          LocalMetadataUri(localRepo, artifact)
        ).toStream
      }
    metadataUris.map(_.uri)
  }

  def loadVersions(artifact: ArtifactWithoutVersion): (ArtifactWithoutVersion, Seq[String]) = {
    // TODO get repository path from ~/.m2/settings.xml or use default
    val uris: Stream[URI] = collectMetadataUris(
      artifact,
      "https://jcenter.bintray.com",
      new File(s"${System.getProperty("user.home")}/.m2/repository")
    )

    val opt: Option[(ArtifactWithoutVersion, Seq[String])] = uris.map { uri =>
      Try {
        val versions = MavenMetadataVersionsRawReader.versionsDesc(XML.load(uri.toURL))
        println(uri)
        versions
      }
        .toOption
        .map { versions => artifact -> versions }
    }.collectFirst {
      case Some(t) => t
    }
    opt.get // TODO refactoring
  }

  def loadLatestVersion(artifact: ArtifactWithoutVersion): ArtifactWithVersion = {
    val (newArtifact, versions) = Utils.loadVersions(artifact)
    newArtifact.withVersion(Utils.excludeNonRelease(versions).head)
  }

  def mkArtifact(raw: String): ArtifactWithoutVersion = {
    val Array(a, b) = raw.split(':')
    new ArtifactWithoutVersion(a, b)
  }

  def unshortAll(rawDependencies: List[String], unshort: String => Option[String]): List[ArtifactWithoutVersion] =
    rawDependencies
      .map(raw => unshort(raw).getOrElse(raw))
      .map(mkArtifact)

  def shortcutLineToArtifact(line: String): (String, ArtifactWithoutVersion) = {
    line.split(',') match {
      case Array(short, full) =>
        val Array(a, b) = full.split(":")
        short -> new ArtifactWithoutVersion(a, b)
      case wrongArray => throw new IllegalArgumentException(s"wrong array $wrongArray")
    }
  }
}
