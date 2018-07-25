package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Indent
import ru.d10xa.jadd.Scope.Test
import ru.d10xa.jadd.view.ArtifactView
import ru.d10xa.jadd.view.ArtifactView.Match

trait MavenImplicits {

  implicit val mavenArtifactView: ArtifactView[(Artifact, Indent)] = new ArtifactView[(Artifact, Indent)] {
    override def showLines(artifactAndIndent: (Artifact, Indent)): Seq[String] = {
      val (artifact, indent) = artifactAndIndent
      val indentString = indent.take(1)

      def artifactToString(artifact: Artifact): String = artifact match {
        case a if a.scope.contains(Test) =>
          s"""<dependency>
             |$indentString<groupId>${a.groupId}</groupId>
             |$indentString<artifactId>${a.artifactId}</artifactId>
             |$indentString<version>${a.maybeVersion.get}</version>
             |$indentString<scope>test</scope>
             |</dependency>""".stripMargin
        case a =>
          s"""<dependency>
             |$indentString<groupId>${a.groupId}</groupId>
             |$indentString<artifactId>${a.artifactId}</artifactId>
             |$indentString<version>${a.maybeVersion.get}</version>
             |</dependency>""".stripMargin
      }

      artifactToString(artifact.inlineScalaVersion) :: Nil
    }


    // TODO Extract to typeclass ArtifactFind
    override def find(artifactAndIndent: (Artifact, Indent), source: String): Seq[Match] = ???
  }

}
