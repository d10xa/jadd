package ru.d10xa.jadd.show

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.regex.GradleVerbalExpressions._
import ru.d10xa.jadd.stringinterpolation.GStr

class GradleShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): String = {
    import ru.d10xa.jadd.regex.RegexImplicits._

    val t3 = stringWithGroupIdArtifactIdVersion()
      .groups3(buildFileSource)
      .map { case (g, a, v) => s"$g:$a:$v" }
    val t2 = stringWithGroupIdArtifactId()
      .groups2(buildFileSource)
      .map { case (g, a) => s"$g:$a" }

    val map: Map[String, String] = variableAssignment.build().groups2(buildFileSource).toMap
    val interpolated = GStr.interpolate(map)

    val all = t3 ++ t2
    all.map(new GStr(_)).map(_.resolve(interpolated)).mkString("\n")
  }
}
