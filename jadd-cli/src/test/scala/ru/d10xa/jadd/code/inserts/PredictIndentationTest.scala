package ru.d10xa.jadd.code.inserts

import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.code.Indent
import ru.d10xa.jadd.code.Indentation

class PredictIndentationTest extends TestBase {

  test("single line indentation") {
    import Indent._
    fromCodeLine("   42") shouldEqual Some(space(3))
    fromCodeLine("    42") shouldEqual Some(space(4))
    fromCodeLine("\t\t\t42") shouldEqual Some(tab(3))
    fromCodeLine("    ") shouldEqual None
    fromCodeLine("\t") shouldEqual None
  }

  test("2 spaces") {
    val content =
      """<project>
        |  <artifactId>a</artifactId>
        |  <groupId>a</groupId>
        |  <modelVersion>4.0.0</modelVersion>
        |  <dependencies>
        |    <dependency>
        |      <groupId>junit</groupId>
        |      <artifactId>junit</artifactId>
        |      <version>3.8.1</version>
        |      <scope>test</scope>
        |    </dependency>
        |  </dependencies>
        |</project>
        |""".stripMargin

    val Indent(style, size) =
      Indentation.predictIndentation(content.split("\n").toList)

    style shouldEqual ' '
    size shouldEqual 2
  }

  test("4 spaces and some 2-space indents") {
    val content =
      """<project>
        |    <artifactId>a</artifactId>
        |    <groupId>a</groupId>
        |    <modelVersion>4.0.0</modelVersion>
        |    <dependencies>
        |        <dependency>
        |          <groupId>junit</groupId>
        |          <artifactId>junit</artifactId>
        |          <version>3.8.1</version>
        |          <scope>test</scope>
        |        </dependency>
        |    </dependencies>
        |</project>
        |""".stripMargin

    val Indent(style, size) =
      Indentation.predictIndentation(content.split("\n").toList)

    style shouldEqual ' '
    size shouldEqual 4
  }

  test("2 tabs") {
    val content = StringContext.processEscapes(
      """<project>
        |\t\t<artifactId>a</artifactId>
        |\t\t<groupId>a</groupId>
        |\t\t<modelVersion>4.0.0</modelVersion>
        |\t\t<dependencies>
        |\t\t\t\t<dependency>
        |\t\t\t\t\t\t<groupId>junit</groupId>
        |\t\t\t\t\t\t<artifactId>junit</artifactId>
        |\t\t\t\t\t\t<version>3.8.1</version>
        |\t\t\t\t\t\t<scope>test</scope>
        |\t\t\t\t</dependency>
        |\t\t</dependencies>
        |</project>
        |""".stripMargin
    )

    val Indent(style, size) =
      Indentation.predictIndentation(content.split("\n").toList)

    style shouldEqual '\t'
    size shouldEqual 2
  }

  test("no indents") {
    val content = StringContext.processEscapes(
      """<project>
        |<artifactId>a</artifactId>
        |<groupId>a</groupId>
        |<modelVersion>4.0.0</modelVersion>
        |<dependencies>
        |</dependencies>
        |</project>
        |""".stripMargin
    )

    val Indent(style, size) =
      Indentation.predictIndentation(content.split("\n").toList)

    // some defaults
    style shouldEqual ' '
    size shouldEqual 4
  }

  test("one level indent") {
    val content =
      """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        |  <modelVersion>4.0.0</modelVersion>
        |  <groupId>com.example</groupId>
        |  <artifactId>example-mvn</artifactId>
        |  <packaging>jar</packaging>
        |  <version>1.0-SNAPSHOT</version>
        |  <name>example-mvn</name>
        |  <url>http://maven.apache.org</url>
        |</project>
        |""".stripMargin

    val Indent(style, size) =
      Indentation.predictIndentation(content.split("\n").toList)

    // some defaults
    style shouldEqual ' '
    size shouldEqual 2
  }
}
