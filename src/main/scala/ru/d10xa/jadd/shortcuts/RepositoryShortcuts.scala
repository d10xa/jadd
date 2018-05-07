package ru.d10xa.jadd.shortcuts

trait RepositoryShortcuts {
  def unshortRepository(repo: String): String
}

object RepositoryShortcutsImpl extends RepositoryShortcuts {

  override def unshortRepository(repo: String): String = {
    if (repo.startsWith("bintray/")) s"https://dl.bintray.com/${repo.drop(8)}"
    else if (repo.startsWith("sonatype/")) s"https://oss.sonatype.org/content/repositories/${repo.drop(9)}"
    else if (repo == "mavenCentral") "http://central.maven.org/maven2"
    else if (repo == "jcenter") "https://jcenter.bintray.com"
    else if (repo == "google") "https://dl.google.com/dl/android/maven2"
    else repo
  }

}
