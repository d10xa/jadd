package ru.d10xa.jadd.inserts

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Indentation

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

    def dependency =
      """<dependency>
        |  <groupId>ch.qos.logback</groupId>
        |  <artifactId>logback-classic</artifactId>
        |  <version>1.2.3</version>
        |</dependency>""".stripMargin

    val lines = content.split('\n')
    val result = MavenFileInserts.append(
      lines,
      List(dependency.split('\n')),
      Indentation.predictIndentation(lines)
    ).mkString("\n")

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

  test("add dependency to pom.xml without dependencies tag with indent 2 spaces") {
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

    def dependency =
      """<dependency>
        |  <groupId>ch.qos.logback</groupId>
        |  <artifactId>logback-classic</artifactId>
        |  <version>1.2.3</version>
        |</dependency>""".stripMargin

    val lines = content.split('\n')
    val result = MavenFileInserts.append(
      lines,
      List(dependency.split('\n')),
      Indentation.predictIndentation(lines)
    ).mkString("\n")

    result shouldEqual
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
        |      <groupId>ch.qos.logback</groupId>
        |      <artifactId>logback-classic</artifactId>
        |      <version>1.2.3</version>
        |    </dependency>
        |  </dependencies>
        |</project>""".stripMargin
  }

  test("add dependency to pom.xml without dependencies tag with indent 1 tab") {
    val content = StringContext.treatEscapes(
      """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |\txsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        |\t<modelVersion>4.0.0</modelVersion>
        |\t<groupId>com.example</groupId>
        |\t<artifactId>example-mvn</artifactId>
        |\t<packaging>jar</packaging>
        |\t<version>1.0-SNAPSHOT</version>
        |\t<name>example-mvn</name>
        |\t<url>http://maven.apache.org</url>
        |</project>
        |""".stripMargin
    )

    def dependency = StringContext.treatEscapes(
      """<dependency>
        |\t<groupId>ch.qos.logback</groupId>
        |\t<artifactId>logback-classic</artifactId>
        |\t<version>1.2.3</version>
        |</dependency>""".stripMargin
    )

    val lines = content.split('\n')
    val result = MavenFileInserts.append(
      lines,
      List(dependency.split('\n')),
      Indentation.predictIndentation(lines)
    ).mkString("\n")

    result shouldEqual StringContext.treatEscapes(
      """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |\txsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        |\t<modelVersion>4.0.0</modelVersion>
        |\t<groupId>com.example</groupId>
        |\t<artifactId>example-mvn</artifactId>
        |\t<packaging>jar</packaging>
        |\t<version>1.0-SNAPSHOT</version>
        |\t<name>example-mvn</name>
        |\t<url>http://maven.apache.org</url>
        |\t<dependencies>
        |\t\t<dependency>
        |\t\t\t<groupId>ch.qos.logback</groupId>
        |\t\t\t<artifactId>logback-classic</artifactId>
        |\t\t\t<version>1.2.3</version>
        |\t\t</dependency>
        |\t</dependencies>
        |</project>""".stripMargin
    )

  }

}
