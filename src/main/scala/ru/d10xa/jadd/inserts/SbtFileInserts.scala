package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.pipelines.SbtPipeline
import ru.d10xa.jadd.view.ArtifactView

class SbtFileInserts {

  import ArtifactView._
  import SbtPipeline._

  def append[T](buildFileSource: String, artifacts: Seq[Artifact]): String = {
    val (updates, inserts) = artifacts.map(a => a -> a.find(buildFileSource)).span(_._2.isDefined)

    val insertStrings = inserts.map(_._1).flatMap(_.showLines)
    val appendResult = appendLines(buildFileSource.split('\n'), insertStrings).mkString("\n") + "\n"

    updates.foldLeft(appendResult) {
      case (source, (artifact, Some(foundString))) =>
        source.replaceAllLiterally(foundString, artifact.showLines.mkString("\n"))
    }
  }

  def appendLines(buildFileLines: Array[String], dependencies: Seq[String]): Seq[String] =
    buildFileLines.toVector ++ dependencies
}
