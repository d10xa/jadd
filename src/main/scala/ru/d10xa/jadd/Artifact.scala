package ru.d10xa.jadd

final case class Artifact(
  groupId: String,
  artifactId: String,
  maybeVersion: Option[String] = None, // TODO list of versions
  shortcut: Option[String] = None,
  scope: Option[Scope] = None,
  repositoryPath: Option[String] = None,
  maybeScalaVersion: Option[String] = None
) {

  def needScalaVersionResolving: Boolean = artifactId.contains("%%")

  def asPath: String = {
    val groupIdPath = groupId.replace('.', '/')
    val art =
      if (needScalaVersionResolving && maybeScalaVersion.isDefined) artifactIdWithScalaVersion(maybeScalaVersion.get)
      else artifactId
    val l: Seq[String] = groupIdPath :: art :: Nil
    maybeVersion
      .map(l :+ _)
      .getOrElse(l)
      .mkString("/")
  }

  def artifactIdWithScalaVersion(v: String): String = {
    require(artifactId.contains("%%"), "scala version resolving require placeholder %%")
    artifactId.replace("%%", s"_$v")
  }

}
