package ru.d10xa.jadd.versions

import coursier.core.Version

trait VersionFilter {
  def excludeNonRelease(versions: Seq[Version]): Seq[Version]
}

object VersionFilter extends VersionFilter {
  override def excludeNonRelease(versions: Seq[Version]): Seq[Version] = {
    val exclude = Seq("rc", "alpha", "beta", "m")
    val filteredVersions =
      versions.filter { version =>
        !exclude.exists(version.repr.toLowerCase.contains(_))
      }
    if (filteredVersions.isEmpty) versions else filteredVersions
  }
}
