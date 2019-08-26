package ru.d10xa.jadd.repository

import ru.d10xa.jadd.testkit.TestBase

class RepositoryApiTest extends TestBase {

  test("rtrim trailing slash") {
    import RepositoryApi.rtrimSlash
    rtrimSlash("http://localhost:8080/") shouldEqual "http://localhost:8080"
    rtrimSlash("http://localhost:8080") shouldEqual "http://localhost:8080"
    rtrimSlash("http://localhost:8080/something/") shouldEqual "http://localhost:8080/something"
    rtrimSlash("http://localhost:8080/something") shouldEqual "http://localhost:8080/something"
  }

  test("metadata with max version") {
    val a = MavenMetadata(versions = Seq("1.6.1"))
    val b = MavenMetadata(versions = Seq("2.0.0", "1.6.1"))
    val c = MavenMetadata(versions = Seq("2.0.0", "1.6.1"))
    val d = MavenMetadata(versions = Seq.empty[String])
    RepositoryApi
      .metadataWithMaxVersion(Seq(a, b, c, d))
      .shouldBe(Some(b))
  }

}
