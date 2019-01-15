package ru.d10xa.jadd.it

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Jadd
import ru.d10xa.jadd.testkit.BuildFileTestBase
import ru.d10xa.jadd.testkit.WireMockTestBase

class MainGradleTest
    extends WireMockTestBase
    with BuildFileTestBase
    with FunSuiteLike
    with Matchers {

  override def buildFileName: String = "build.gradle"

  test("install dependency") {
    write(
      """
        |dependencies {
        |    compile "commons-io:commons-io:2.6"
        |}
      """.stripMargin
    )

    Jadd.main(
      Array(
        "install",
        "-q",
        projectDirArg,
        "--repository",
        mockedRepositoryUrl,
        "junit:junit"))

    read() shouldEqual
      """
        |dependencies {
        |    compile "commons-io:commons-io:2.6"
        |    testCompile "junit:junit:4.12"
        |}
      """.stripMargin
  }

  test("install scala dependency") {
    write(
      """
        |dependencies {
        |    compile "commons-io:commons-io:2.6"
        |}
      """.stripMargin
    )

    Jadd.main(
      Array(
        "install",
        "-q",
        projectDirArg,
        "--repository",
        mockedRepositoryUrl,
        "io.circe:circe-generic%%"))

    read() shouldEqual
      """
        |dependencies {
        |    compile "commons-io:commons-io:2.6"
        |    compile "io.circe:circe-generic_2.12:0.9.3"
        |}
      """.stripMargin
  }

}
