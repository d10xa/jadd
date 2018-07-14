package ru.d10xa.jadd.show

import org.scalatest.FunSuite
import org.scalatest.Matchers

class GradleShowTest extends FunSuite with Matchers {

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

    val show = new GradleShowCommand(source).show()

    val expected: String =
      """a:b:1.0
        |com.example42:y:2.3.4-SNAPSHOT
        |e:h:9.9.9
        |org.springframework.boot:spring-boot-starter-web""".stripMargin

    show shouldEqual expected
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

    val show = new GradleShowCommand(source).show()

    val expected: String =
      """org.scala-lang:scala-library:2.12.6
        |org.scalatest:scalatest_2.12:3.0.4""".stripMargin

    show shouldEqual expected
  }

}
