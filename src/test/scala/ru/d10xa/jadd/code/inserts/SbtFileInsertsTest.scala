package ru.d10xa.jadd.code.inserts

import cats.effect._
import coursier.core.Version
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.testkit.FilesF
import ru.d10xa.jadd.testkit.TestBase
import cats.syntax.all._

class SbtFileInsertsTest extends TestBase {

  def assertUpserts[F[_]: Sync](
    files: Seq[(String, String)],
    upsert: Seq[Artifact],
    expectInsert: Seq[Artifact],
    expectUpdateVersions: Seq[String]
  ): F[Unit] = {
    implicit val logger: Logger[F] = Logger.make[F](
      debug = false,
      quiet = true
    )
    implicit val filesF: FilesF[F] = new FilesF[F]
    tempSbtFileUpsertsResource[F](files: _*)
      .use { case (path, fileOps, sbtFileUpserts) =>
        sbtFileUpserts.upsert(upsert)
      }
      .flatTap { upsertQuery =>
        Sync[F].delay(
          upsertQuery.toInsert should contain theSameElementsAs expectInsert
        )
      }
      .flatTap { upsertQuery =>
        Sync[F].delay(
          upsertQuery.toUpdate.view.toVector.flatMap(_._2).map(_._2.repr)
            should contain theSameElementsAs expectUpdateVersions
        )
      }
      .void
  }

  test("insert") {
    val buildSbtContent =
      """libraryDependencies += "org.typelevel" %% "cats-core" % "2.6.0",
        |""".stripMargin
    val a1 = Artifact(
      groupId = GroupId("ch.qos.logback"),
      artifactId = "logback-classic",
      maybeVersion = Some(Version("1.2.3"))
    )
    assertUpserts[SyncIO](
      files = Seq("build.sbt" -> buildSbtContent),
      upsert = Seq(a1),
      expectInsert = Seq(a1),
      expectUpdateVersions = Seq()
    ).unsafeRunSync()
  }

  test("update") {
    val buildSbtContent =
      """libraryDependencies ++= Seq(
        |  "a" %% "scala_lib" % "1.0.1",
        |  "a" % "java_lib" % "2.3"
        |)
        |""".stripMargin
    val scalaLib = Artifact(
      groupId = GroupId("a"),
      artifactId = "scala_lib",
      maybeVersion = Some(Version("1.0.2")),
      maybeScalaVersion = Some(ScalaVersion.fromString("2.12"))
    )
    val javaLib = Artifact(
      groupId = GroupId("a"),
      artifactId = "java_lib",
      maybeVersion = Some(Version("2.4"))
    )
    assertUpserts[SyncIO](
      files = Seq("build.sbt" -> buildSbtContent),
      upsert = Seq(scalaLib, javaLib),
      expectInsert = Seq(),
      expectUpdateVersions = Seq("1.0.2", "2.4")
    ).unsafeRunSync()
  }

  test("update another file") {
    val dependenciesContent =
      """object Dependencies {
        |  val aVersion = "1.0"
        |}
        |""".stripMargin
    val buildSbtContent =
      """
        |libraryDependencies ++= Seq(
        |  "a" %% "aa" % Dependencies.aVersion
        |)
        |""".stripMargin
    val a1 = Artifact(
      groupId = GroupId("a"),
      artifactId = "aa",
      maybeVersion = Some(Version("1.1")),
      maybeScalaVersion = Some(ScalaVersion.fromString("2.12"))
    )
    assertUpserts[SyncIO](
      files = Seq(
        "build.sbt" -> buildSbtContent,
        "project/Dependencies.sbt" -> dependenciesContent
      ),
      upsert = Seq(a1),
      expectInsert = Seq(),
      expectUpdateVersions = Seq("1.1")
    ).unsafeRunSync()
  }

}
