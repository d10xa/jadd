import xerial.sbt.Sonatype._

lazy val commonSettings = Seq(
  organization := "ru.d10xa",
  scalaVersion := "2.13.12",
  licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))
)

lazy val pgpSettings = Seq(
  pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray),
  pgpSigningKey := sys.env.get("GPG_KEYID")
)

lazy val publishSettings = Seq(
  publishTo := sonatypePublishTo.value,
  sonatypeProjectHosting := Some(
    GitHubHosting("d10xa", "jadd", "Andrey Stolyarov", "d10xa@mail.ru")
  )
)

lazy val `root` = project
  .in(file("."))
  .aggregate(
    `jadd-core`.jvm,
    `jadd-cli`,
    `jadd-it`,
    `jadd-parser-sbt`.jvm
  )
  .settings(
    publish / skip := true
  )

lazy val `jadd-parser-sbt` = crossProject(JSPlatform, JVMPlatform)
  .in(file("jadd-parser-sbt"))
  .dependsOn(`jadd-core`)
  .settings(commonSettings, pgpSettings, publishSettings)
  .settings(
    libraryDependencies ++= Seq(
      ("org.scalameta" %%% "scalameta" % "4.8.15")
        .cross(CrossVersion.for3Use2_13)
    )
  )

lazy val `jadd-core` = crossProject(JSPlatform, JVMPlatform)
  .in(file("jadd-core"))
  .settings(
    commonSettings,
    pgpSettings,
    publishSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "org.typelevel" %%% "cats-effect" % "3.5.3",
      "dev.optics" %%% "monocle-core" % "3.2.0",
      "dev.optics" %%% "monocle-macro" % "3.2.0",
      ("io.get-coursier" %%% "coursier-core" % "2.0.16")
        .cross(CrossVersion.for3Use2_13),
      "eu.timepit" %%% "refined" % "0.10.1"
    )
  )

lazy val `jadd-js` = project
  .in(file("jadd-js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`jadd-core`.js)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := false
  )

lazy val `jadd-cli` = project
  .in(file("jadd-cli"))
  .dependsOn(`jadd-parser-sbt`.jvm)
  .settings(commonSettings, pgpSettings, publishSettings)
  .settings(
    Compile / mainClass := Some("ru.d10xa.jadd.Jadd"),
    description := "Command-line tool for adding dependencies to gradle/maven/sbt build files",
    libraryDependencies ++= {
      scalaVersion.value match {
        case s if s.startsWith("3") =>
          Nil
        case _ =>
          List(
            compilerPlugin(
              ("org.typelevel" % "kind-projector" % "0.13.2").cross(
                CrossVersion.full
              )
            )
          )
      }
    },
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "4.1.0",
      "org.jline" % "jline" % "3.25.0",
      "com.lihaoyi" %% "ujson" % "3.1.4",
      "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.8",
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "org.jsoup" % "jsoup" % "1.17.2",
      "org.antlr" % "antlr4-runtime" % "4.13.1", // ???
      "com.47deg" %% "github4s" % "0.32.1",
      "io.lemonlabs" %% "scala-uri" % "4.0.3",
      "org.http4s" %% "http4s-blaze-client" % "0.23.15",
      ("io.get-coursier" %% "coursier" % "2.0.16").cross(
        CrossVersion.for3Use2_13
      ),
      "com.lihaoyi" %% "pprint" % "0.8.1",
      ("com.github.pathikrit" %% "better-files" % "3.9.2")
        .cross(CrossVersion.for3Use2_13),
      "com.github.tomakehurst" % "wiremock" % "3.0.1" % Test
    ),
    scalacOptions := Seq(
      "-encoding",
      "UTF-8", // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked", // warn about unchecked type parameters
      "-feature", // warn about misused language features
      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
      "-Xlint" // enable handy linter warnings
      //      "-Xfatal-warnings", // turn compiler warnings into errors,
    ),
    scalacOptions ++= (if (scalaVersion.value.startsWith("3"))
                         Seq("-explain-types", "-Ykind-projector")
                       else Seq("-explaintypes", "-Wunused"))
  )

lazy val `jadd-it` = project
  .in(file("jadd-it"))
  .settings(commonSettings)
  .dependsOn(`jadd-cli` % "compile->compile;test->test")
  .settings(
    publish / skip := true,
    scalacOptions := Seq(
      "-encoding",
      "UTF-8", // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked", // warn about unchecked type parameters
      "-feature", // warn about misused language features
      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
      "-Xlint" // enable handy linter warnings
    )
  )
