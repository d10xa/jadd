package ru.d10xa.jadd.regex

import org.scalatest.FunSuite
import org.scalatest.Matchers

class GradleVerbalExpressionsTest extends FunSuite with Matchers {

  import ru.d10xa.jadd.regex.RegexImplicits._

  val source: String =
    """
      |plugins {
      |    id 'java'
      |}
      |repositories {
      |    jcenter()
      |}
      |dependencies {
      |    compile 'a:b:1.0'
      |    compile("com.example42:y:2.3.4-SNAPSHOT") // no space parenthesis
      |    testCompile ('e:h:9.9.9') // space and parenthesis
      |    testCompile group: 'x.y.z', name: 'a-b', version: '1.42.1'
      |    compile "org.springframework.boot:spring-boot-starter-web"
      |    testCompile("org.springframework.boot:spring-boot-starter-test") // no space parenthesis
      |    testCompile ("org.springframework.boot:spring-boot-starter-actuator") // space and parenthesis
      |}
      |// comment
    """.stripMargin

  test("stringWithGroupIdArtifactIdVersion") {

    val ve = GradleVerbalExpressions.stringWithGroupIdArtifactIdVersion()

    val groups3: Seq[(String, String, String)] = ve.groups3(source)

    groups3 shouldEqual Seq(
      ("a", "b", "1.0"),
      ("com.example42", "y", "2.3.4-SNAPSHOT"),
      ("e", "h", "9.9.9")
    )
  }

  test("stringWithGroupIdArtifactId") {

    val ve = GradleVerbalExpressions.stringWithGroupIdArtifactId()

    val groups2: Seq[(String, String)] = ve.groups2(source)

    groups2 shouldEqual Seq(
      ("org.springframework.boot", "spring-boot-starter-web"),
      ("org.springframework.boot", "spring-boot-starter-test"),
      ("org.springframework.boot", "spring-boot-starter-actuator")
    )
  }

}
