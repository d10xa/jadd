package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.show.SbtFormatShowPrinter
import ru.d10xa.jadd.view.ArtifactView

trait SbtImplicits {

  implicit val sbtArtifactView: ArtifactView[Artifact] =
    (artifact: Artifact) => {
      SbtFormatShowPrinter.single(artifact) :: Nil
    }

}
