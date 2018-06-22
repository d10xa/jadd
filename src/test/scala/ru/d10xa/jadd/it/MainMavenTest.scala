package ru.d10xa.jadd.it

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Jadd
import ru.d10xa.jadd.testkit.BuildFileTestBase

class MainMavenTest extends BuildFileTestBase("pom.xml") with FunSuiteLike with Matchers {

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

    Jadd.main(Array("install", "-q", projectDirArg, "ch.qos.logback:logback-core"))

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
