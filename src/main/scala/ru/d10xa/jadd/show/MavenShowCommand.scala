package ru.d10xa.jadd.show

import cats.data.Chain
import com.typesafe.scalalogging.StrictLogging
import coursier.core.Version
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId

import scala.xml.Node
import scala.xml.XML

class MavenShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): Chain[Artifact] = {
    val xml = XML.loadString(buildFileSource)
    def node2artifacts(n: Node): Artifact = {
      val versionNode = n \ "version"
      val v =
        if (versionNode.nonEmpty) Some(Version(versionNode.text)) else None
      Artifact(
        groupId = GroupId((n \ "groupId").text),
        artifactId = (n \ "artifactId").text,
        maybeVersion = v
      )
    }
    Chain
      .fromSeq(xml \\ "project" \\ "dependencies" \\ "dependency")
      .map(node2artifacts)
  }
}
