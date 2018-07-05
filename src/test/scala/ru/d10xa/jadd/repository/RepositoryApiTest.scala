package ru.d10xa.jadd.repository

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

class RepositoryApiTest extends FunSuiteLike with Matchers {

  test("rtrim trailing slash") {
    RepositoryApi.rtrimSlash("http://localhost:8080/") shouldEqual "http://localhost:8080"
    RepositoryApi.rtrimSlash("http://localhost:8080") shouldEqual "http://localhost:8080"
    RepositoryApi.rtrimSlash("http://localhost:8080/something/") shouldEqual "http://localhost:8080/something"
    RepositoryApi.rtrimSlash("http://localhost:8080/something") shouldEqual "http://localhost:8080/something"
  }

}
