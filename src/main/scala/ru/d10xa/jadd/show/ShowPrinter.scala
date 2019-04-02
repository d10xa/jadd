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
