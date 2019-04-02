package ru.d10xa.jadd.show

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.Artifact

import scala.xml.Node
import scala.xml.XML

class MavenShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): Seq[Artifact] = {
    val xml = XML.loadString(buildFileSource)
    def node2artifacts(n: Node): Artifact = {
      val versionNode = n \ "version"
      val v = if (versionNode.nonEmpty) Some(versionNode.text) else None
      Artifact(
        groupId = (n \ "groupId").text,
        artifactId = (n \ "artifactId").text,
        maybeVersion = v
      )
    }
    (xml \\ "project" \\ "dependencies" \\ "dependency")
      .map(node2artifacts)
  }
}