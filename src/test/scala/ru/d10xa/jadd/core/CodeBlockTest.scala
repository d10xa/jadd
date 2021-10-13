package ru.d10xa.jadd.core

import ru.d10xa.jadd.testkit.TestBase

class CodeBlockTest extends TestBase {

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
    find(
      buildFileSource,
      "dependencies {"
    ).head.innerContent shouldEqual expected
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
    val blockContent = find(buildFileSource, "dependencies {").head
    blockContent.innerContent shouldEqual expected
    buildFileSource.substring(blockContent.innerStartIndex) should startWith(
      " // }\n    testCompile 'junit:junit:4.11'"
    )
  }

  test("block not found") {
    def buildFileSource =
      """repositories {
         |    jcenter()
         |}
         |""".stripMargin
    find(buildFileSource, "dependencies {").size shouldEqual 0
  }

}
