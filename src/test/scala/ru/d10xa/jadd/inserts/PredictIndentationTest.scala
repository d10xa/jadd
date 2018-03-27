package ru.d10xa.jadd.inserts

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Indentation

class PredictIndentationTest extends FunSuite with Matchers {

  test("single line indentation") {
    import Indentation._
    lineIndentation("   42") shouldEqual Some(' ' -> 3)
    lineIndentation("    42") shouldEqual Some(' ' -> 4)
    lineIndentation("\t\t\t42") shouldEqual Some('\t' -> 3)
    lineIndentation("    ") shouldEqual None
    lineIndentation("\t") shouldEqual None
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

    val (character, count) = Indentation.predictIndentation(content.split("\n").toList)

    character shouldEqual ' '
    count shouldEqual 2
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

    val (character, count) = Indentation.predictIndentation(content.split("\n").toList)

    character shouldEqual ' '
    count shouldEqual 4
  }

  test("2 tabs") {
    val content = StringContext.treatEscapes(
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

    val (character, count) = Indentation.predictIndentation(content.split("\n").toList)

    character shouldEqual '\t'
    count shouldEqual 2
  }

  test("no indents") {
    val content = StringContext.treatEscapes(
      """<project>
        |<artifactId>a</artifactId>
        |<groupId>a</groupId>
        |<modelVersion>4.0.0</modelVersion>
        |<dependencies>
        |</dependencies>
        |</project>
        |""".stripMargin
    )

    val (character, count) = Indentation.predictIndentation(content.split("\n").toList)

    // some defaults
    character shouldEqual ' '
    count shouldEqual 4
  }
}
