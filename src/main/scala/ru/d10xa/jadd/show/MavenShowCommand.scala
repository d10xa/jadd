package ru.d10xa.jadd.show

import com.typesafe.scalalogging.StrictLogging

import scala.xml.Node
import scala.xml.XML

class MavenShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): String = {
    val xml = XML.loadString(buildFileSource)
    def parseDependencyNode(n: Node): (String, String, Option[String]) = {
      val versionNode = n \ "version"
      val v = if (versionNode.nonEmpty) Some(versionNode.text) else None
      ((n \ "groupId").text, (n \ "artifactId").text, v)
    }
    (xml \\ "project" \\ "dependencies" \\ "dependency")
      .map(parseDependencyNode)
      .map({
        case (g, a, Some(v)) =>
          s"$g:$a:$v"
        case (g, a, None) =>
          s"$g:$a"
      })
      .mkString("\n")
  }
}
