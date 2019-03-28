package ru.d10xa.jadd.inserts

import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.view.ArtifactView

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
      |    testCompile 'e:h:9.9.9'
      |    testCompile group: 'x.y.z', name: 'a-b', version: '1.42.1'
      |}
      |// comment
    """.stripMargin

  test("basic dependency with compile configuration") {

    val matcher = new GradleArtifactMatcher(source)

    val match1: Seq[ArtifactView.Match] = matcher.find(art("a:b"))
    match1.size shouldEqual 1
    match1.head.value shouldEqual """compile 'a:b:1.0'"""
    match1.head.start shouldEqual source.indexOf("""compile 'a:b:1.0'""")

  }

  test("basic dependency with testCompile configuration") {

    val matcher = new GradleArtifactMatcher(source)

    val match1: Seq[ArtifactView.Match] = matcher.find(art("e:h"))
    match1.size shouldEqual 1
    match1.head.value shouldEqual """testCompile 'e:h:9.9.9'"""
    match1.head.start shouldEqual source.indexOf("""testCompile 'e:h:9.9.9'""")

  }

}
