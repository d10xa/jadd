package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.experimental.CodeBlock
import ru.d10xa.jadd.view.ArtifactView.Match

import scala.util.matching.Regex

class GradleArtifactMatcher(source: String) {
  def find(artifact: Artifact): Seq[Match] = {
    val blocks: Seq[CodeBlock] = CodeBlock.find(source, "dependencies {")
    def inBlock(m: Match): Boolean = m.inBlock(blocks)

    val possibleArtifactIds = if (artifact.isScala) {
      Seq(
        artifact.artifactIdWithoutScalaVersion,
        artifact.artifactIdWithScalaVersion("2.11"),
        artifact.artifactIdWithScalaVersion("2.12")
      )
    } else Seq(artifact.artifactId)

    def makeRegexes(artifactIds: Seq[String]): Seq[Regex] = {
      artifactIds.flatMap { artifactId =>
        Seq(
          raw"""(compile|testCompile)\s+['"]${artifact.groupId}:$artifactId:([\w\d\._-]+)['"]""".r,
          raw"""(compile|testCompile)\s+group:\s+['"]${artifact.groupId}['"],\s+name:\s+['"]$artifactId['"],\s+version:\s+['"]([\w\d\._-]+)['"]""".r
        )
      }
    }

    Match.find(source, makeRegexes(possibleArtifactIds))
      .filter(inBlock)
  }
}
