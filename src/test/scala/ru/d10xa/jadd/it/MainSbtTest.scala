package ru.d10xa.jadd.it

import ru.d10xa.jadd.Jadd
import ru.d10xa.jadd.testkit.BuildFileTestBase
import ru.d10xa.jadd.testkit.WireMockTestBase

class MainSbtTest extends WireMockTestBase with BuildFileTestBase {

  override def buildFileName: String = "build.sbt"

  test("update dependency") {
    write(
      """
        |libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.0"
      """.stripMargin
    )

    Jadd.main(
      Array(
        "install",
        "-q",
        projectDirArg,
        "--repository",
        mockedRepositoryUrl,
        "logback-core"
      )
    )

    read() shouldEqual
      """
        |libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
      """.stripMargin
  }

}
