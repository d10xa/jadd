package ru.d10xa.jadd.implicits

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.show.GradleFormatShowPrinter
import ru.d10xa.jadd.show.GradleLang.Groovy
import ru.d10xa.jadd.view.ArtifactView

trait GradleImplicits {

  implicit val gradleArtifactView: ArtifactView[Artifact] =
    (artifact: Artifact) => {
      new GradleFormatShowPrinter(Groovy) // TODO add kotlin
        .single(artifact.inlineScalaVersion) :: Nil
    }

}
