package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.experimental.CodeBlock
import ru.d10xa.jadd.view.ArtifactView.Match

import scala.util.matching.Regex

class SbtArtifactMatcher(source: String) {

  def find(artifact: Artifact): Seq[Match] = {

    def findBlockContent(s: String): Seq[CodeBlock] = CodeBlock.find(source, s)
    def findBlockContents(s: String*): Seq[CodeBlock] = s.map(findBlockContent).reduce(_ ++ _)
    val blocks: Seq[CodeBlock] = findBlockContents(
      "libraryDependencies ++= Seq(",
      "libraryDependencies ++= Seq (",
      "libraryDependencies ++= List(",
      "libraryDependencies ++= List (",
      "libraryDependencies ++= Vector(",
      "libraryDependencies ++= Vector ("
    ).distinct
    def inBlock(m: Match): Boolean = m.inBlock(blocks)

    findInSequence(artifact).filter(inBlock) ++ findStandalone(artifact).filterNot(inBlock)
  }

  def r0(artifact: Artifact): String = {
    val optionalScalaVersion = "(_2.11)?(_2.12)?"
    raw"""["']${artifact.groupId}["']\s%\s["']${artifact.artifactIdWithoutScalaVersion}$optionalScalaVersion["']\s%\s["'].+["'](\s%\sTest)?"""
  }
  def r1(artifact: Artifact): String = raw"""["']${artifact.groupId}["']\s%%\s["']${artifact.artifactIdWithoutScalaVersion}["']\s%\s["'].+["'](\s%\sTest)?"""
  def r2(artifact: Artifact, scalaVersion: String): String = {
    val aId = artifact.artifactIdWithScalaVersion(scalaVersion)
    raw"""["']${artifact.groupId}["']\s%\s["']$aId["']\s%\s["'].+["'](\s%\sTest)?"""
  }

  def findInSequence(artifact: Artifact): Seq[Match] = {
    def regex0: Seq[Regex] = Seq(r0(artifact).r)
    def regex1: Seq[Regex] = Seq(r1(artifact).r)
    def regex2: Seq[Regex] = (artifact.isScala, artifact.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) => Seq(r2(artifact, scalaVersion).r)
      case _ => Seq.empty
    }
    findMatches(regex0 ++ regex1 ++ regex2)
      .filterNot(isCommented)
      .map(_.copy(inSequence = true))
  }

  def findMatches(regexes: Seq[Regex]): Seq[Match] = Match.find(source, regexes)

  def isCommented(m: Match): Boolean = {
    source
      .substring(0, m.start)
      .reverse
      .takeWhile(_ != '\n')
      .contains("//")
  }

  def findStandalone(artifact: Artifact): Seq[Match] = {

    def regex0: Seq[Regex] = Seq(raw"""libraryDependencies\s\+=\s${r0(artifact)}""".r)

    def regex1: Seq[Regex] = Seq(raw"""libraryDependencies\s\+=\s${r1(artifact)}""".r)

    def regex2: Seq[Regex] = (artifact.isScala, artifact.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) =>
        Seq(raw"""libraryDependencies\s\+=\s${r2(artifact, scalaVersion)}""".r)
      case _ => Seq.empty
    }

    findMatches(regex0 ++ regex1 ++ regex2)
      .filterNot(isCommented)
      .map(_.copy(inSequence = false))

  }

}
