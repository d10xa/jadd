package ru.d10xa.jadd.code.inserts

import ru.d10xa.jadd.testkit.TestBase

class GradleArtifactMatcherTest extends TestBase {

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
      |    compile 'x:y:2.3.4'
      |    testCompile "e:h:9.9.9"
      |    testCompile group: 'x.y.z', name: 'a-b', version: '1.42.1'
      |    implementation group: "i", name: "j", version: "12"
      |}
      |// comment
    """.stripMargin

  test("basic dependency with compile configuration and single quotes") {

    val matcher = new GradleArtifactMatcher(source)

    val match1 = matcher.find(art("a:b"))
    match1.size shouldEqual 1
    match1.head.value shouldEqual """compile 'a:b:1.0'"""
    match1.head.start shouldEqual source.indexOf("""compile 'a:b:1.0'""")
    match1.head.doubleQuotes shouldEqual false
    match1.head.configuration shouldBe "compile"
  }

  test("basic dependency with testCompile configuration and double quotes") {

    val matcher = new GradleArtifactMatcher(source)

    val match1 = matcher.find(art("e:h"))
    match1.size shouldEqual 1
    match1.head.value shouldEqual """testCompile "e:h:9.9.9""""
    match1.head.start shouldEqual source.indexOf("""testCompile "e:h:9.9.9"""")
    match1.head.doubleQuotes shouldEqual true
    match1.head.configuration shouldEqual "testCompile"
  }

  test("basic dependency with implementation configuration and double quotes") {

    val matcher = new GradleArtifactMatcher(source)

    val match1 = matcher.find(art("i:j"))
    match1.size shouldEqual 1
    match1.head.value shouldEqual """implementation group: "i", name: "j", version: "12""""
    match1.head.start shouldEqual source.indexOf(
      """implementation group: "i", name: "j", version: "12""""
    )
    match1.head.doubleQuotes shouldEqual true
    match1.head.configuration shouldEqual "implementation"
  }

}
