package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers

import scala.xml.XML

class MavenMetadataVersionsRawReaderTest extends FunSuite with Matchers {

  val resource = getClass.getResourceAsStream("/maven-metadata/scala-maven-metadata.xml")
  val text = scala.io.Source.fromInputStream(resource).mkString
  val elem = XML.loadString(text)

  test("parse xml") {

    val version =
      MavenMetadata.read(elem)
        .versions
        .reverse
        .toStream
        .head

    version shouldEqual "2.13.0-M3"
  }

  test("read lastUpdated pretty format") {
    val lastUpdated = MavenMetadataVersionsRawReader.lastUpdated(elem)

    lastUpdated shouldEqual Some("2018-01-31 21:47:39")
  }

  test("read lastUpdated strange format") {

    val xmlString: String =
      """|<?xml version="1.0" encoding="UTF-8"?>
         |<metadata>
         |  <versioning>
         |    <lastUpdated>42</lastUpdated>
         |  </versioning>
         |</metadata>
      """.stripMargin
    val lastUpdated = MavenMetadataVersionsRawReader.lastUpdated(XML.loadString(xmlString))

    lastUpdated shouldEqual Some("42")
  }

  test("lastUpdated absent") {

    val xmlString: String =
      """|<?xml version="1.0" encoding="UTF-8"?>
         |<metadata>
         |  <versioning>
         |  </versioning>
         |</metadata>
      """.stripMargin
    val lastUpdated = MavenMetadataVersionsRawReader.lastUpdated(XML.loadString(xmlString))

    lastUpdated shouldEqual None
  }

}
