package ru.d10xa.jadd.inserts

import cats.data.NonEmptyList
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.experimental.CodeBlock
import ru.d10xa.jadd.regex.GradleVerbalExpressions
import ru.d10xa.jadd.versions.ScalaVersions.supportedMinorVersions
import ru.d10xa.jadd.view.ArtifactView.GradleMatchImpl
import ru.d10xa.jadd.view.ArtifactView.Match

import scala.util.matching.Regex

class GradleArtifactMatcher(source: String) {
  def find(artifact: Artifact): Seq[GradleMatchImpl] = {
    val blocks: Seq[CodeBlock] = CodeBlock.find(source, "dependencies {")
    def inBlock(m: Match): Boolean = m.inBlock(blocks)
    def possibleVersionsWithScala: NonEmptyList[String] =
      supportedMinorVersions
        .map(v => artifact.artifactIdWithScalaVersion(v))
    val possibleArtifactIds = if (artifact.isScala) {
      possibleVersionsWithScala
    } else NonEmptyList.of(artifact.artifactId)

    val configurations =
      GradleVerbalExpressions.gradleAllowedConfigurations.mkString("|")

    def makeRegexes(artifactIds: NonEmptyList[String]): NonEmptyList[Regex] =
      artifactIds.flatMap { artifactId =>
        NonEmptyList.of(
          raw"""($configurations)\s+(['"])${artifact.groupId}:$artifactId:([\w\d\._-]+)['"]""".r,
          raw"""($configurations)\s+group:\s+(['"])${artifact.groupId}['"],\s+name:\s+['"]$artifactId['"],\s+version:\s+['"]([\w\d\._-]+)['"]""".r
        )
      }

    def findInRegexes(
      source: String,
      regexes: NonEmptyList[Regex]): Seq[GradleMatchImpl] =
      for {
        regex <- regexes.toList
        m <- regex.findAllMatchIn(source)
      } yield
        GradleMatchImpl(
          start = m.start,
          value = m.group(0),
          configuration = m.group(1),
          doubleQuotes = m.group(2) == "\"")

    findInRegexes(source, makeRegexes(possibleArtifactIds))
      .filter(inBlock)
  }
}
