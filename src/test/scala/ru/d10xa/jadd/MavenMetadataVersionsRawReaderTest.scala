package ru.d10xa.jadd

import java.io.InputStream

import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.repository.MavenMetadata
import ru.d10xa.jadd.xml.MavenMetadataVersionsRawReader

import scala.xml.Elem
import scala.xml.XML

class MavenMetadataVersionsRawReaderTest extends TestBase {

  val resource: InputStream =
    getClass.getResourceAsStream("/maven-metadata/scala-maven-metadata.xml")
  val text: String = scala.io.Source.fromInputStream(resource).mkString
  val elem: Elem = XML.loadString(text)

  test("parse xml") {

    val version =
      MavenMetadata
        .readFromXml(MavenMetadata(), elem)
        .versions
        .reverse
        .head
    version shouldEqual "2.13.0-M3"
  }

  test("read lastUpdated") {
    val lastUpdated = MavenMetadataVersionsRawReader.lastUpdated(elem)

    lastUpdated shouldEqual Some("20180131214739")
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
    val lastUpdated =
      MavenMetadataVersionsRawReader.lastUpdated(XML.loadString(xmlString))

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
    val lastUpdated =
      MavenMetadataVersionsRawReader.lastUpdated(XML.loadString(xmlString))

    lastUpdated shouldEqual None
  }

}
