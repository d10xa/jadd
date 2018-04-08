package ru.d10xa.jadd

import org.scalatest._

class MavenMetadataVersionsRawReaderTest extends FlatSpec with Matchers {

  "parse xml" should "not fail" in {
    val resource = getClass.getResourceAsStream("/maven-metadata/scala-maven-metadata.xml")
    val text = scala.io.Source.fromInputStream(resource).mkString

    val exclude = Seq("rc", "alpha", "beta", "m", ".r")

    val version =
      MavenMetadataVersionsRawReader
        .xmlContentToVersionsDesc(text)
        .toStream
        .filter { version => !exclude.exists(version.toLowerCase.contains(_)) }
        .head

    version shouldEqual "2.12.4"

  }
}
