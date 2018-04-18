package ru.d10xa.jadd.inserts

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class GradleFileInsertsTest extends FlatSpec with Matchers {
  "build file with nonempty dependencies block" should "successfully add dependency" in {

    def buildFileSource =
   """|apply plugin: 'java'
      |repositories {
      |    jcenter()
      |}
      |dependencies {
      |    testCompile 'junit:junit:4.12'
      |}
      |""".stripMargin

    val result = new GradleFileInserts().append(
      buildFileSource,
      List(
        "compile 'org.codehaus.groovy:groovy-all:2.4.14'",
        "compile 'org.springframework.boot:spring-boot-starter-web:2.0.0.RELEASE'"
      )
    )

    result shouldEqual List(
      "apply plugin: 'java'",
      "repositories {",
      "    jcenter()",
      "}",
      "dependencies {",
      "    compile 'org.codehaus.groovy:groovy-all:2.4.14'",
      "    compile 'org.springframework.boot:spring-boot-starter-web:2.0.0.RELEASE'",
      "    testCompile 'junit:junit:4.12'",
      "}"
    )
  }

  "if build file indented with tabs" should "add dependency with tabs" in {
    val content = StringContext.treatEscapes(
      """apply plugin: 'java'
        |repositories {
        |\tjcenter()
        |}
        |dependencies {
        |\ttestCompile 'junit:junit:4.12'
        |}
      """.stripMargin
    )

    val newContent = new GradleFileInserts()
      .append(content, List("compile 'org.codehaus.groovy:groovy-all:2.4.14'"))

    newContent should contain ("\tcompile 'org.codehaus.groovy:groovy-all:2.4.14'")
  }

}
