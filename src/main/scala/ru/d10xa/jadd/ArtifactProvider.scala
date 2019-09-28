package ru.d10xa.jadd

import ru.d10xa.jadd.regex.GradleVerbalExpressions.stringWithGroupIdArtifactId
import ru.d10xa.jadd.regex.GradleVerbalExpressions.stringWithGroupIdArtifactIdVersion
import ru.d10xa.jadd.regex.GradleVerbalExpressions.variableAssignment
import ru.d10xa.jadd.stringinterpolation.GStr

trait ArtifactProvider[T] {
  def provide(t: T): Seq[Artifact]
}

object ArtifactProvider {
  final case class GradleBuildDescription(buildFileSource: String)

  implicit val gradleArtifactProvider
    : ArtifactProvider[GradleBuildDescription] =
    (d: GradleBuildDescription) => {

      import ru.d10xa.jadd.regex.RegexImplicits._
      val map: Map[String, String] =
        variableAssignment.build().groups2(d.buildFileSource).toMap
      val interpolated = GStr.interpolate(map)

      val t3 = stringWithGroupIdArtifactIdVersion()
        .groups3(d.buildFileSource)
        .map { case (g, a, v) => s"$g:$a:$v" }
      val t2 = stringWithGroupIdArtifactId()
        .groups2(d.buildFileSource)
        .map { case (g, a) => s"$g:$a" }

      val all = t3 ++ t2

      all
        .map(new GStr(_).resolve(interpolated))
        .map(Artifact.fromString)
        .flatMap(_.toOption)
    }

  implicit class BuildDescriptionImplicits[T](val buildDescription: T)(
    implicit artifactProvider: ArtifactProvider[T]) {
    def artifacts: Seq[Artifact] = artifactProvider.provide(buildDescription)
  }

}
