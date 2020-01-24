package ru.d10xa.jadd.code.regex

import ru.d10xa.jadd.testkit.TestBase

class SbtVerbalExpressionsTest extends TestBase {
  test("testDeclaredDependency") {
    import RegexImplicits._
    val source =
      """
        |import sbt._
        |object Dependencies {
        |  lazy val junit = "junit" % "junit" % "4.12"
        |  lazy val catsCore = "org.typelevel" %% "cats-core" % "1.1.0"
        |}
        |""".stripMargin
    val verbalExpression = SbtVerbalExpressions.declaredDependency
    val tuples = verbalExpression.groups4(source)
    tuples shouldEqual ("junit", "%", "junit", "4.12") :: (
      "org.typelevel",
      "%%",
      "cats-core",
      "1.1.0") :: Nil
  }

}
