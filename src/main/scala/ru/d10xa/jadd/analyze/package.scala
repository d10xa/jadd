package ru.d10xa.jadd

import cats.data.EitherT
import cats.implicits._
import ru.d10xa.jadd.Utils.SimpleMetadataUri
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl

package object analyze {
  def run(ctx: Ctx): Unit = {
    val config = ctx.config
    val repositoryShortcuts = RepositoryShortcutsImpl
    val artifactShortcuts = new ArtifactShortcuts()
    val artifactInfoFinder = new ArtifactInfoFinder(artifactShortcuts, repositoryShortcuts)
    val artifacts = EitherT(config.artifacts.map(artifactInfoFinder.artifactFromString).toList)

    val defaultRepos = List("https://jcenter.bintray.com", "https://repo1.maven.org/maven2")
    val reposFromConfig: List[String] = ctx.config.repositories.map(repositoryShortcuts.unshortRepository).toList
    val repos: List[String] = if(reposFromConfig.nonEmpty) reposFromConfig else defaultRepos

    def loadVersions(a: Artifact, repo: String): Either[troubles.ArtifactTrouble, Artifact] =
      Utils.loadVersions(a, SimpleMetadataUri(repo, a))

    val r: EitherT[List, troubles.ArtifactTrouble, Artifact] =
      artifacts.flatMap(a => EitherT(repos.map(repo => loadVersions(a, repo))))

    def artifactAnalyzeAsString(a: Artifact): String = {
      val s1 = Seq(
        s" repository: ${a.repository.getOrElse("-")}",
        s" lastUpdated: ${a.mavenMetadata.flatMap(_.lastUpdated).getOrElse("-")}",
        //        s" shortcut: ${a.shortcut.getOrElse("-")}",
        s" metadata: ${a.mavenMetadata.flatMap(_.url).getOrElse("-")}",
        " versions:"
      )
      val s2 = a.availableVersions.map(v => s"  $v")
      (s1 ++ s2).mkString("\n")
    }

    val value: List[Either[troubles.ArtifactTrouble, Artifact]] = r.value
    val (ts, as) = value.separate

    as.groupBy(a => s"${a.groupId}:${a.artifactId}").foreach {
      case (artifactShow, groupedArtifacts) =>
        println(s"## $artifactShow")
        groupedArtifacts.foreach(a => println(artifactAnalyzeAsString(a)))
    }

    if(ts.nonEmpty) println("ERRORS:")
    ts.foreach(t => troubles.handleTroubles(Seq(t), println))

  }
}
