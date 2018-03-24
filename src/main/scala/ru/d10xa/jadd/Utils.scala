package ru.d10xa.jadd

import java.net.URL

import scala.util.Try
import scala.xml.XML

object Utils {

  def excludeNonRelease(versions: Seq[String]): Seq[String] = {
    val exclude = Seq("rc", "alpha", "beta", "m") //, ".r")
    versions.filter { version => !exclude.exists(version.toLowerCase.contains(_)) }
  }

  def loadVersions(artifact: ArtifactWithoutVersion): (ArtifactWithoutVersion, Seq[String]) = {
    val groupIdPath = artifact.groupId.replace('.', '/')
    val needScalaVersionResolving = artifact.artifactId.contains("%%")

    if (!needScalaVersionResolving) {
      val artifactId = artifact.artifactId
      val url = new URL(s"""https://jcenter.bintray.com/$groupIdPath/$artifactId/maven-metadata.xml""")
      println(s"load $url")
      val elem = XML.load(url)
      artifact -> MavenMetadataVersionsRawReader.versionsDesc(elem)
    } else {
      val scalaVersions = Seq("2.12", "2.11")
      def urlWithVersion(v: String): String = {
        val artifactWithVersion = artifact.artifactId.replace("%%", s"_$v")
        s"""https://jcenter.bintray.com/$groupIdPath/$artifactWithVersion/maven-metadata.xml"""
      }

      val (scalaVersion, versions) = scalaVersions
        .map { v: String => Try(v -> MavenMetadataVersionsRawReader.versionsDesc(XML.load(urlWithVersion(v)))) }
        .reduce(_ orElse _)
        .get

      artifact.copy(artifactId = artifact.artifactId.replace("%%", s"_$scalaVersion")) -> versions
    }
  }

  def loadLatestVersion(artifact: ArtifactWithoutVersion): ArtifactWithVersion = {
    val (newArtifact, versions) = Utils.loadVersions(artifact)
    newArtifact.withVersion(Utils.excludeNonRelease(versions).head)
  }

  def mkArtifact(raw: String): ArtifactWithoutVersion = {
    val Array(a, b) = raw.split(':')
    ArtifactWithoutVersion(a, b)
  }

  def unshortAll(rawDependencies: List[String], unshort: String => Option[String]): List[ArtifactWithoutVersion] =
    rawDependencies
      .map(raw => unshort(raw).getOrElse(raw))
      .map(mkArtifact)

  def shortcutLineToArtifact(line: String): (String, ArtifactWithoutVersion) = {
    line.split(',') match {
      case Array(short, full) =>
        val Array(a, b) = full.split(":")
        short -> ArtifactWithoutVersion(a, b)
      case wrongArray => throw new IllegalArgumentException(s"wrong array $wrongArray")
    }
  }
}
