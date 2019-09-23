package ru.d10xa.jadd.it

import ru.d10xa.jadd.Jadd
import ru.d10xa.jadd.testkit.BuildFileTestBase
import ru.d10xa.jadd.testkit.WireMockTestBase

class MainMavenTest extends WireMockTestBase with BuildFileTestBase {

  override def buildFileName: String = "pom.xml"

  test("insert dependency") {
    val content =
      """
        |<project>
        |    <modelVersion>4.0.0</modelVersion>
        |    <artifactId>a</artifactId>
        |    <dependencies>
        |        <dependency>
        |            <groupId>junit</groupId>
        |            <artifactId>junit</artifactId>
        |            <version>4.12</version>
        |            <scope>test</scope>
        |        </dependency>
        |    </dependencies>
        |</project>
        |""".stripMargin

    write(content)

    Jadd.main(
      Array(
        "install",
        "-q",
        projectDirArg,
        "--repository",
        mockedRepositoryUrl,
        "ch.qos.logback:logback-core",
        "org.testng:testng"
      )
    )

    val expected =
      """
        |<project>
        |    <modelVersion>4.0.0</modelVersion>
        |    <artifactId>a</artifactId>
        |    <dependencies>
        |        <dependency>
        |            <groupId>ch.qos.logback</groupId>
        |            <artifactId>logback-core</artifactId>
        |            <version>1.2.3</version>
        |        </dependency>
        |        <dependency>
        |            <groupId>org.testng</groupId>
        |            <artifactId>testng</artifactId>
        |            <version>6.14.3</version>
        |            <scope>test</scope>
        |        </dependency>
        |        <dependency>
        |            <groupId>junit</groupId>
        |            <artifactId>junit</artifactId>
        |            <version>4.12</version>
        |            <scope>test</scope>
        |        </dependency>
        |    </dependencies>
        |</project>
        |""".stripMargin

    read() shouldEqual expected
  }

  test("m2") {
    val content =
      """
        |<project>
        |    <modelVersion>4.0.0</modelVersion>
        |    <artifactId>a</artifactId>
        |    <dependencies>
        |        <dependency>
        |            <groupId>junit</groupId>
        |            <artifactId>junit</artifactId>
        |            <version>4.12</version>
        |            <scope>test</scope>
        |        </dependency>
        |    </dependencies>
        |</project>
        |""".stripMargin

    write(content)

    Jadd.main(
      Array(
        "install",
        "-q",
        projectDirArg,
        "--repository",
        "src/test/resources/m2/repository",
        "com.example:projectname"
      ))

    val expected =
      """
        |<project>
        |    <modelVersion>4.0.0</modelVersion>
        |    <artifactId>a</artifactId>
        |    <dependencies>
        |        <dependency>
        |            <groupId>com.example</groupId>
        |            <artifactId>projectname</artifactId>
        |            <version>12.5</version>
        |        </dependency>
        |        <dependency>
        |            <groupId>junit</groupId>
        |            <artifactId>junit</artifactId>
        |            <version>4.12</version>
        |            <scope>test</scope>
        |        </dependency>
        |    </dependencies>
        |</project>
        |""".stripMargin

    read() shouldEqual expected
  }

}
