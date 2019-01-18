package ru.d10xa.jadd.versions

import cats.data.EitherNel
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles.ArtifactTrouble

trait VersionTools {

  // TODO api refactoring

  def excludeNonRelease(versions: Seq[String]): Seq[String]
  def loadLatestVersion(
    artifact: Artifact): EitherNel[ArtifactTrouble, Artifact]
}

object VersionTools extends VersionTools {

  override def loadLatestVersion(
    artifact: Artifact): EitherNel[ArtifactTrouble, Artifact] =
    artifact
      .loadVersions()
      .map(_.initLatestVersion())

  override def excludeNonRelease(versions: Seq[String]): Seq[String] = {
    val exclude = Seq("rc", "alpha", "beta", "m")
    val filteredVersions =
      versions.filter { version =>
        !exclude.exists(version.toLowerCase.contains(_))
      }
    if (filteredVersions.isEmpty) versions else filteredVersions
  }

}
