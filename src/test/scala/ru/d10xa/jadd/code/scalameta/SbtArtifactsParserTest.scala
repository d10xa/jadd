package ru.d10xa.jadd.code.scalameta

import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching._
import ru.d10xa.jadd.testkit.TestBase

class SbtArtifactsParserTest extends TestBase {

  test("extractValues") {
    val scope = Scope(
      name = Some(value = "x"),
      items = Vector(Value(path = Vector("v"), value = "1"))
    )
    val (changes, vector) = SbtArtifactsParser.extractValues(Vector(scope))
    changes shouldBe 1
    vector shouldBe Vector(
      Value(path = Vector("x", "v"), value = "1"),
      Scope(name = Some(value = "x"), items = Vector())
    )
  }
  test("extractModules") {
    val scope =
      Scope(
        name = Some(value = "junit"),
        items = Vector(
          Module(
            groupId = LitString(value = "junit"),
            percentsCount = 1,
            artifactId = LitString(value = "junit"),
            version = LitString(value = "4.12"),
            terms = List()
          )
        )
      )
    val (changes, vector) = SbtArtifactsParser.extractModules(Vector(scope))
    changes shouldBe 1
    vector shouldBe Vector(
      Module(
        groupId = LitString(value = "junit"),
        percentsCount = 1,
        artifactId = LitString(value = "junit"),
        version = LitString(value = "4.12"),
        terms = List()
      ),
      Scope(name = Some(value = "junit"), items = Vector())
    )

  }

}
