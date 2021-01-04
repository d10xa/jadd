package ru.d10xa.jadd.requirements

import cats.effect.IO
import ru.d10xa.jadd.testkit.TestBase
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.pipelines.Pipeline

class RequirementsTest extends TestBase {
  test("read requirements") {
    val artifacts = Pipeline
      .extractArtifacts[IO](
        Ctx(
          Config(
            artifacts = Seq("junit"),
            requirements = Seq("classpath:jrequirements/jrequirements.txt")
          )
        )
      )
      .unsafeRunSync()
    val Seq(a, b, c) = artifacts
    a shouldEqual "commons-csv"
    b shouldEqual "commons-io"
    c shouldEqual "com.google.code.gson:gson:2.8.5"
  }
}
