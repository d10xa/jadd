package ru.d10xa.jadd.experimental

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

class CodeBlockTest extends FunSuiteLike with Matchers {

  import CodeBlock._

  test("simple extract code block") {
    def buildFileSource =
      """|apply plugin: 'java'
         |dependencies {
         |    testCompile 'junit:junit:4.11'
         |}
         |repositories {
         |    jcenter()
         |}
         |""".stripMargin
    def expected =
      """
        |    testCompile 'junit:junit:4.11'
        |""".stripMargin
    extractBlockContent(buildFileSource, "dependencies {").head._2 shouldEqual expected
  }

  test("extract code block with single line comments") {
    def buildFileSource =
      """|apply plugin: 'java'
         |dependencies { // }
         |    testCompile 'junit:junit:4.11'
         |}
         |repositories {
         |    jcenter()
         |}
         |""".stripMargin
    def expected =
      """ // }
        |    testCompile 'junit:junit:4.11'
        |""".stripMargin
    extractBlockContent(buildFileSource, "dependencies {").head._2 shouldEqual expected
  }

  test("block not found") {
    def buildFileSource =
      """repositories {
         |    jcenter()
         |}
         |""".stripMargin
    extractBlockContent(buildFileSource, "dependencies {").size shouldEqual 0
  }

  test("sbt dependencies seq") {
    def buildFileSource =
      """
        |// some code here
        |libraryDependencies ++= Seq(
        |  "ch.qos.logback" % "logback-classic" % "1.2.3"
        |)
        |// some code here
        |""".stripMargin
    extractBlockContent(buildFileSource, "libraryDependencies ++= Seq(").head._2 shouldEqual
      """
        |  "ch.qos.logback" % "logback-classic" % "1.2.3"
        |""".stripMargin
  }

}
