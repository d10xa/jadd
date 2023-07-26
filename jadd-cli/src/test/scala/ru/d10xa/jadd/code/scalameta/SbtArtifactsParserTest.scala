package ru.d10xa.jadd.code.scalameta

import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching._
import ru.d10xa.jadd.testkit.TestBase

import java.nio.file.Paths
import scala.meta.inputs.Position

class SbtArtifactsParserTest extends TestBase {

  test("extractValues") {
    val scope = Scope(
      name = Some(value = "x"),
      items = Vector(
        Value(
          path = Vector("v"),
          value = "1",
          pos = Position.None,
          Paths.get(".")
        )
      ),
      filePath = None
    )
    val (changes, vector) = SbtModuleParserLoopReduce
      .extractValues(Vector(scope))
    changes shouldBe 1
    vector shouldBe Vector(
      Value(
        path = Vector("x", "v"),
        value = "1",
        Position.None,
        Paths.get(".")
      ),
      Scope(name = Some(value = "x"), items = Vector(), filePath = None)
    )
  }
  test("extractModules") {
    val scope =
      Scope(
        name = Some(value = "junit"),
        items = Vector(
          Module(
            groupId = VariableLit(value = "junit", Position.None),
            percentsCount = 1,
            artifactId = VariableLit(value = "junit", Position.None),
            version = VariableLit(value = "4.12", Position.None),
            terms = List()
          )
        ),
        filePath = None
      )
    val (changes, vector) = SbtModuleParserLoopReduce
      .extractModules(Vector(scope))
    changes shouldBe 1
    vector shouldBe Vector(
      Module(
        groupId = VariableLit(value = "junit", Position.None),
        percentsCount = 1,
        artifactId = VariableLit(value = "junit", Position.None),
        version = VariableLit(value = "4.12", Position.None),
        terms = List()
      ),
      Scope(name = Some(value = "junit"), items = Vector(), filePath = None)
    )

  }

}
