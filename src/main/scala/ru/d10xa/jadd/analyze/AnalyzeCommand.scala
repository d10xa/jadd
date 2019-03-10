package ru.d10xa.jadd.analyze

import cats.data._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.Utils
import ru.d10xa.jadd.troubles
import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.repository.RepositoryApi
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl

trait AnalyzeCommand {
  def run(ctx: Ctx): Unit
}

class AnalyzeCommandImpl extends AnalyzeCommand with StrictLogging {

  override def run(ctx: Ctx): Unit = {
    val config = ctx.config
    val repositoryShortcuts = RepositoryShortcutsImpl
    val artifactShortcuts =
      new ArtifactShortcuts(Utils.sourceFromSpringUri(ctx.config.shortcutsUri))
    val artifactInfoFinder =
      new ArtifactInfoFinder(artifactShortcuts, repositoryShortcuts)
    val artifacts =
      config.artifacts.map(artifactInfoFinder.artifactFromString).toList

    val defaultRepos =
      List("https://jcenter.bintray.com", "https://repo1.maven.org/maven2")
    val reposFromConfig: List[String] =
      ctx.config.repositories.map(repositoryShortcuts.unshortRepository).toList
    val repos: List[String] =
      if (reposFromConfig.nonEmpty) reposFromConfig else defaultRepos

    def loadVersions(
      a: Artifact,
      repo: String): EitherNel[troubles.ArtifactTrouble, Artifact] = {
      val errorOrMeta: EitherNel[troubles.MetadataLoadTrouble, MavenMetadata] =
        RepositoryApi.fromString(repo).receiveRepositoryMeta(a)
      errorOrMeta.map(meta => a.merge(meta))
    }

    val r: EitherT[List, NonEmptyList[troubles.ArtifactTrouble], Artifact] =
      EitherT(artifacts.map(_.leftMap(NonEmptyList.one)))
        .flatMap(a => EitherT(repos.map(repo => loadVersions(a, repo))))

    def artifactAnalyzeAsString(a: Artifact): String = {
      val s1 = Seq(
        s" repository: ${a.repository.getOrElse("-")}",
        s" lastUpdated: ${a.mavenMetadata.flatMap(_.lastUpdatedPretty).getOrElse("-")}",
        //        s" shortcut: ${a.shortcut.getOrElse("-")}",
        s" metadata: ${a.mavenMetadata.flatMap(_.url).getOrElse("-")}",
        " versions:"
      )
      val s2 = a.availableVersions.map(v => s"  $v")
      (s1 ++ s2).mkString("\n")
    }

    val value: List[Either[NonEmptyList[troubles.ArtifactTrouble], Artifact]] =
      r.value
    val (ts, as) = value.separate

    as.groupBy(a => s"${a.groupId}:${a.artifactId}").foreach {
      case (artifactShow, groupedArtifacts) =>
        logger.info(s"## $artifactShow")
        groupedArtifacts.foreach(a => logger.info(artifactAnalyzeAsString(a)))
    }

    if (ts.nonEmpty) logger.info("ERRORS:")
    // TODO t.toList ???
    ts.foreach(t => troubles.handleTroubles(t.toList, s => logger.info(s)))

  }

}
