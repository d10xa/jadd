package ru.d10xa.jadd.it

import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.repository.MavenRemoteMetadataRepositoryApi
import ru.d10xa.jadd.testkit.WireMockTestBase

class MavenRemoteMetadataRepositoryApiTest extends WireMockTestBase {

  def api = new MavenRemoteMetadataRepositoryApi(mockedRepositoryUrl)

  test("simple metadata read") {
    val meta =
      api
        .receiveRepositoryMetaWithMaxVersion(
          art("ch.qos.logback:logback-classic"))
        .toOption
        .get
    meta.versions.head shouldEqual "0.2.5"
    meta.versions.last shouldEqual "1.3.0-alpha4"
    meta.versions.size shouldEqual 74
    meta.lastUpdatedPretty shouldEqual Some("2018-02-11 21:58:39")
    meta.url shouldEqual Some(
      s"$mockedRepositoryUrl/ch/qos/logback/logback-classic/maven-metadata.xml")
  }

  test("artifact for scala 2.11 if newer missing") {
    val meta =
      api
        .receiveRepositoryMetaWithMaxVersion(
          art("org.apache.spark:spark-core%%"))
        .toOption
        .get
    meta.versions.last shouldEqual "2.3.1"
  }

  test("artifact with specific version of scala if available 2.11 and 2.12") {
    val artifact = Artifact(
      groupId = GroupId("io.circe"),
      artifactId = "circe-generic%%",
      maybeScalaVersion = Some(ScalaVersion.fromString("2.11")))
    val meta = api.receiveRepositoryMetaWithMaxVersion(artifact).toOption.get
    meta.lastUpdated.get shouldEqual "20180521123606"
  }

}
