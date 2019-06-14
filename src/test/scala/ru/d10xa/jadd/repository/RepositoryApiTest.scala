package ru.d10xa.jadd.repository

import ru.d10xa.jadd.testkit.TestBase

class RepositoryApiTest extends TestBase {

  test("rtrim trailing slash") {
    RepositoryApi.rtrimSlash("http://localhost:8080/") shouldEqual "http://localhost:8080"
    RepositoryApi.rtrimSlash("http://localhost:8080") shouldEqual "http://localhost:8080"
    RepositoryApi.rtrimSlash("http://localhost:8080/something/") shouldEqual "http://localhost:8080/something"
    RepositoryApi.rtrimSlash("http://localhost:8080/something") shouldEqual "http://localhost:8080/something"
  }

}
