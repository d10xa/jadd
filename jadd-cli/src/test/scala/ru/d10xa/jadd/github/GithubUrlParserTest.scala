package ru.d10xa.jadd.github

import ru.d10xa.jadd.github.GithubUrlParser.GithubUrlParts
import ru.d10xa.jadd.testkit.TestBase
import cats.implicits._

import scala.util.Success
import scala.util.Try

class GithubUrlParserTest extends TestBase {

  val parser: GithubUrlParser[Try] =
    LiveGithubUrlParser.make[Try]()

  test("file") {
    parser
      .parse("https://github.com/d10xa/jadd/blob/master/project/plugins.sbt")
      .shouldBe(
        Success(
          GithubUrlParts(
            "d10xa",
            "jadd",
            Some("project/plugins.sbt"),
            Some("master")
          )
        )
      )
  }

  test("repo root") {
    parser
      .parse("https://github.com/d10xa/jadd")
      .shouldBe(Success(GithubUrlParts("d10xa", "jadd", None, None)))
  }
}
