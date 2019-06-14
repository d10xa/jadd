package ru.d10xa.jadd.repository

import ru.d10xa.jadd.testkit.TestBase

class MavenMetadataTest extends TestBase {

  test("lastUpdated pretty format") {
    val result = MavenMetadata.lastUpdatedPretty("20180131214739")
    result shouldEqual "2018-01-31 21:47:39"
  }

  test("lastUpdated strange format as is") {
    val result = MavenMetadata.lastUpdatedPretty("12345")
    result shouldEqual "12345"
  }

}
