package ru.d10xa.jadd.it

import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.repository.MavenRemoteMetadataRepositoryApi
import ru.d10xa.jadd.testkit.WireMockTestBase

class MavenRemoteMetadataRepositoryApiTest extends WireMockTestBase {

  def api = new MavenRemoteMetadataRepositoryApi(mockedRepositoryUrl)

  test("simple metadata read") {
    val meta = api.receiveRepositoryMeta(Artifact("ch.qos.logback:logback-classic")).right.get
    meta.versions.head shouldEqual "0.2.5"
    meta.versions.last shouldEqual "1.3.0-alpha4"
    meta.versions.size shouldEqual 74
    meta.lastUpdatedPretty shouldEqual Some("2018-02-11 21:58:39")
    meta.url shouldEqual Some(s"$mockedRepositoryUrl/ch/qos/logback/logback-classic/maven-metadata.xml")
  }

  test("artifact for scala 2.11 if newer missing") {
    val meta = api.receiveRepositoryMeta(Artifact("org.apache.spark:spark-core%%")).right.get
    meta.versions.last shouldEqual "2.3.1"
  }

  test("artifact with specific version of scala if available 2.11 and 2.12") {
    val artifact = Artifact(
      groupId = "io.circe",
      artifactId = "circe-generic%%", maybeScalaVersion = Some("2.11"))
    val meta = api.receiveRepositoryMeta(artifact).right.get
    meta.lastUpdated.get shouldEqual "20180521123606"
  }

}
