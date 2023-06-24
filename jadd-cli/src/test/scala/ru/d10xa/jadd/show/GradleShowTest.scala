package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class GradleShowTest extends TestBase {

  test("single project") {
    val source: String =
      """
        |plugins {
        |    id 'java'
        |}
        |repositories {
        |    jcenter()
        |}
        |dependencies {
        |    compile 'a:b:1.0' // hello
        |    compile "com.example42:y:2.3.4-SNAPSHOT"
        |    compile "org.springframework.boot:spring-boot-starter-web"
        |    testCompile 'e:h:9.9.9'
        |}
      """.stripMargin

    val show = new GradleShowCommand(source).show().toList

    val expected = Seq(
      art("a:b:1.0"),
      art("com.example42:y:2.3.4-SNAPSHOT"),
      art("e:h:9.9.9"),
      art("org.springframework.boot:spring-boot-starter-web")
    )
    (show should contain).theSameElementsAs(expected)
  }

  test("string interpolation") {
    val source: String =
      """
        |plugins {
        |    id 'scala'
        |}
        |ext {
        |    scalaTestVersion = '3.0.4'
        |}
        |repositories {
        |    jcenter()
        |}
        |def scalaMajorVersion = "2.12"
        |def scalaVersion = "${scalaMajorVersion}.6"
        |dependencies {
        |    compile "org.scala-lang:scala-library:${scalaVersion}"
        |    testCompile "org.scalatest:scalatest_${scalaMajorVersion}:$scalaTestVersion"
        |}
      """.stripMargin

    val show = new GradleShowCommand(source).show().toList

    val expected = Seq(
      art("org.scala-lang:scala-library:2.12.6"),
      art("org.scalatest:scalatest_2.12:3.0.4")
    )

    (show should contain).theSameElementsAs(expected)
  }

}
