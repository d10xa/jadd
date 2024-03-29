package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase

class MavenShowCommandTest extends TestBase {
  test("show") {
    val pom =
      """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        |  <modelVersion>4.0.0</modelVersion>
        |  <groupId>com.example</groupId>
        |  <artifactId>example-mvn</artifactId>
        |  <packaging>jar</packaging>
        |  <version>1.0-SNAPSHOT</version>
        |  <name>example-mvn</name>
        |  <url>http://maven.apache.org</url>
        |  <dependencies>
        |    <dependency>
        |      <groupId>a.b.c</groupId>
        |      <artifactId>d-e</artifactId>
        |      <version>1.2.3</version>
        |    </dependency>
        |    <dependency>
        |      <groupId>junit</groupId>
        |      <artifactId>junit</artifactId>
        |      <version>4.12</version>
        |      <scope>test</scope>
        |    </dependency>
        |  </dependencies>
        |</project>""".stripMargin
    val artifacts = new MavenShowCommand(pom).show().toList
    val expected = Seq(
      art("a.b.c:d-e:1.2.3"),
      art("junit:junit:4.12")
    ).toList
    artifacts shouldEqual expected
  }
}
