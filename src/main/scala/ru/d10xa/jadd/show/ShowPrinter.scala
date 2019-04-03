package ru.d10xa.jadd.show

import ru.d10xa.jadd.Artifact

sealed trait ShowPrinter {
  def mkString(artifacts: List[Artifact]): String
}

object JaddFormatShowPrinter extends ShowPrinter {
  def single(a: Artifact): String = {
    val artifactId: String = (a.isScala, a.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) =>
        a.artifactIdWithScalaVersion(scalaVersion)
      case _ => a.artifactId
    }
    a.maybeVersion match {
      case Some(v) => s"${a.groupId}:$artifactId:$v"
      case None => s"${a.groupId}:$artifactId"
    }
  }
  override def mkString(artifacts: List[Artifact]): String =
    artifacts.map(single).mkString("\n")
}

object AmmoniteFormatShowPrinter extends ShowPrinter {
  def single(a: Artifact): String = {
    val artifactId: String = (a.isScala, a.maybeScalaVersion) match {
      case (true, Some(_)) =>
        s":${a.artifactIdWithoutScalaVersion}"
      case _ => a.artifactId
    }
    val moduleId = a.maybeVersion match {
      case Some(v) => s"${a.groupId}:$artifactId:$v"
      case None => s"${a.groupId}:$artifactId"
    }
    s"import $$ivy.`$moduleId`"
  }
  override def mkString(artifacts: List[Artifact]): String =
    artifacts.map(single).mkString("\n")
}

object GroovyFormatShowPrinter extends ShowPrinter {
  def single(a: Artifact): String = {
    val artifactId: String = (a.isScala, a.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) =>
        a.artifactIdWithScalaVersion(scalaVersion)
      case _ => a.artifactId
    }
    val versionKeyValue = a.maybeVersion match {
      case Some(version) => s", version = '$version'"
      case None => ""
    }
    s"@Grab(group='${a.groupId}', module='$artifactId'$versionKeyValue)"
  }
  override def mkString(artifacts: List[Artifact]): String =
    artifacts.map(single).mkString("\n")
}

object LeiningenFormatShowPrinter extends ShowPrinter {
  def single(a: Artifact): String = {
    val artifactId: String = (a.isScala, a.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) =>
        a.artifactIdWithScalaVersion(scalaVersion)
      case _ => a.artifactId
    }
    val version = a.maybeVersion.getOrElse("LATEST")
    val moduleId = (a.groupId, artifactId) match {
      case (gId, aId) if gId == aId => aId
      case (gId, aId) => s"$gId/$aId"
    }
    s"""[$moduleId "$version"]"""
  }
  override def mkString(artifacts: List[Artifact]): String =
    artifacts.map(single).mkString("\n")
}

object GradleFormatShowPrinter extends ShowPrinter {
  def single(a: Artifact): String = {
    val artifactId: String = (a.isScala, a.maybeScalaVersion) match {
      case (true, Some(scalaVersion)) =>
        a.artifactIdWithScalaVersion(scalaVersion)
      case _ => a.artifactId
    }

    val moduleId =
      (Some(a.groupId) :: Some(artifactId) :: a.maybeVersion :: Nil).flatten
        .mkString(":")

    s"implementation '$moduleId'"
  }
  override def mkString(artifacts: List[Artifact]): String =
    artifacts.map(single).mkString("\n")
}
