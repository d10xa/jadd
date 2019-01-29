package ru.d10xa.jadd.requirements

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.Ctx
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.pipelines.Pipeline

class RequirementsTest extends FunSuite with Matchers {
  test("read requirements") {
    val artifacts = Pipeline.extractArtifacts(
      Ctx(
        Config(
          artifacts = Seq("junit"),
          requirements = Seq("classpath:jrequirements/jrequirements.txt"))))
    val Seq(a, b, c) = artifacts
    a shouldEqual "commons-csv"
    b shouldEqual "commons-io"
    c shouldEqual "com.google.code.gson:gson:2.8.5"
  }
}
