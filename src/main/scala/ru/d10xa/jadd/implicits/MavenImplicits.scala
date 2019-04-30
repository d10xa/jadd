package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.Indent
import ru.d10xa.jadd.show.MavenFormatShowPrinter
import ru.d10xa.jadd.view.ArtifactView

object MavenImplicits {

  implicit val mavenArtifactView: ArtifactView[(Artifact, Indent)] =
    (artifactAndIndent: (Artifact, Indent)) => {
      val (artifact, indent) = artifactAndIndent
      MavenFormatShowPrinter.singleWithIndent(artifact, indent) :: Nil
    }

}
