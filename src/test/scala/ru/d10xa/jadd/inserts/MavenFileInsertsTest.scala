package ru.d10xa.jadd.inserts

import org.scalatest.FunSuite
import org.scalatest.Matchers

class MavenFileInsertsTest extends FunSuite with Matchers {

  test("add dependency to pom.xml") {
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
      |  <dependencyManagement>
      |    <dependencies>
      |    </dependencies>
      |  </dependencyManagement>
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

    def dependency = """<dependency>
                       |  <groupId>ch.qos.logback</groupId>
                       |  <artifactId>logback-classic</artifactId>
                       |  <version>1.2.3</version>
                       |</dependency>""".stripMargin

    val result = MavenFileInserts.append(content.split('\n').toList, List(dependency.split('\n').toList)).mkString("\n")

    println(result)

    result shouldEqual """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        |  <modelVersion>4.0.0</modelVersion>
        |  <groupId>com.example</groupId>
        |  <artifactId>example-mvn</artifactId>
        |  <packaging>jar</packaging>
        |  <version>1.0-SNAPSHOT</version>
        |  <name>example-mvn</name>
        |  <url>http://maven.apache.org</url>
        |  <dependencyManagement>
        |    <dependencies>
        |    </dependencies>
        |  </dependencyManagement>
        |  <dependencies>
        |    <dependency>
        |      <groupId>ch.qos.logback</groupId>
        |      <artifactId>logback-classic</artifactId>
        |      <version>1.2.3</version>
        |    </dependency>
        |    <dependency>
        |      <groupId>junit</groupId>
        |      <artifactId>junit</artifactId>
        |      <version>3.8.1</version>
        |      <scope>test</scope>
        |    </dependency>
        |  </dependencies>
        |</project>""".stripMargin

  }

}
