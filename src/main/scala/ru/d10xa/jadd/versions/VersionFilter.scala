package ru.d10xa.jadd.versions

trait VersionFilter {
  def excludeNonRelease(versions: Seq[String]): Seq[String]
}

object VersionFilter extends VersionFilter {
  override def excludeNonRelease(versions: Seq[String]): Seq[String] = {
    val exclude = Seq("rc", "alpha", "beta", "m")
    val filteredVersions =
      versions.filter { version =>
        !exclude.exists(version.toLowerCase.contains(_))
      }
    if (filteredVersions.isEmpty) versions else filteredVersions
  }
}
