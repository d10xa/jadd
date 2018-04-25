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
    val reposFromConfig = ctx.config.repositories.map(repositoryShortcuts.unshortRepository).toList
    val repos: List[String] = reposFromConfig ++ defaultRepos

    def loadVersions(a: Artifact, repo: String): Either[troubles.ArtifactTrouble, Artifact] =
      Utils.loadVersions(a, SimpleMetadataUri(repo, a))

    val r: EitherT[List, troubles.ArtifactTrouble, Artifact] =
      artifacts.flatMap(a=>EitherT(repos.map(repo => loadVersions(a, repo))))

    def artifactAnalyzeAsString(a: Artifact): String = {
      val s1 = Seq(
        s"${a.groupId}:${a.artifactId}",
        s" repository: ${a.repository.getOrElse("-")}",
//        s" shortcut: ${a.shortcut.getOrElse("-")}",
        s" metadata: ${a.metadataUrl.getOrElse("-")}"
      )
      val s2 = a.availableVersions.map(v => s"  $v")
      (s1 ++ s2).mkString("\n")
    }

    r.value.foreach {
      case Left(t) =>
        troubles.handleTroubles(Seq(t), println)
      case Right(a) =>
        println(artifactAnalyzeAsString(a))
    }

  }
}
