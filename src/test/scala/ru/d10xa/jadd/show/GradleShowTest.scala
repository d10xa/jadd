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
        |    testCompile 'e:h:9.9.9'
        |}
      """.stripMargin

    val show = new GradleShowCommand(source).show()

    val expected: String =
      """a:b:1.0
        |com.example42:y:2.3.4-SNAPSHOT
        |e:h:9.9.9""".stripMargin

    show shouldEqual expected
  }

}
