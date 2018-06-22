package ru.d10xa.jadd.it

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Jadd
import ru.d10xa.jadd.testkit.BuildFileTestBase

class MainGradleTest extends BuildFileTestBase("build.gradle") with FunSuiteLike with Matchers {

  test("install dependency"){
    write(
      """
        |dependencies {
        |    compile "commons-io:commons-io:2.6"
        |}
      """.stripMargin)

    Jadd.main(Array("install", "-q", projectDirArg, "junit:junit"))

    read() shouldEqual
      """
        |dependencies {
        |    compile "commons-io:commons-io:2.6"
        |    testCompile "junit:junit:4.12"
        |}
      """.stripMargin
  }

}
