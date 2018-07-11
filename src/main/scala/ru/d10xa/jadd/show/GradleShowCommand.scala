package ru.d10xa.jadd.show

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.regex.GradleVerbalExpressions

class GradleShowCommand(buildFileSource: String) extends StrictLogging {
  def show(): String = {
    import ru.d10xa.jadd.regex.RegexImplicits._

    val ve = GradleVerbalExpressions.stringWithGroupIdArtifactIdVersion()
    val tuples3 = ve.groups3(buildFileSource)

    tuples3.map {
      case (g, a, v) => s"$g:$a:$v"
    }.mkString("\n")
  }
}
