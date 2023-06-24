package ru.d10xa.jadd.shortcuts

import ru.d10xa.jadd.repository.RepositoryConstants

trait RepositoryShortcuts {
  def unshortRepository(repo: String): String
}

object RepositoryShortcutsImpl extends RepositoryShortcuts {

  override def unshortRepository(repo: String): String =
    if (repo.startsWith("bintray/"))
      s"${RepositoryConstants.bintray}/${repo.drop(8)}"
    else if (repo.startsWith("sonatype/"))
      s"${RepositoryConstants.sonatype}/${repo.drop(9)}"
    else if (repo == "mavenCentral") RepositoryConstants.mavenCentral
    else if (repo == "jcenter") RepositoryConstants.jcenter
    else if (repo == "google") RepositoryConstants.google
    else repo

}
