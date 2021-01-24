name := "jadd"
organization in ThisBuild := "ru.d10xa"

scalaVersion in ThisBuild := "2.13.4"
mainClass in Compile := Some("ru.d10xa.jadd.Jadd")
licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))
description := "Command-line tool for adding dependencies to gradle/maven/sbt build files"

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(
  GitHubHosting("d10xa", "jadd", "Andrey Stolyarov", "d10xa@mail.ru")
)

publishTo := sonatypePublishTo.value

pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)
pgpSigningKey := sys.env.get("GPG_KEYID")

lazy val root = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    scalacOptions := Seq(
      "-encoding",
      "UTF-8", // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked", // warn about unchecked type parameters
      "-feature", // warn about misused language features
      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
      "-Xlint", // enable handy linter warnings
//      "-Xfatal-warnings", // turn compiler warnings into errors,
      "-Ymacro-annotations" // for @newtype
    )
  )

addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % "0.11.3").cross(CrossVersion.full)
)

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.0.0",
  "org.typelevel" %% "cats-core" % "2.3.1",
  "org.jline" % "jline" % "3.19.0",
  "com.lihaoyi" %% "ujson" % "1.2.2",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.6",
  "org.scalatest" %% "scalatest" % "3.2.3" % "it,test",
  "com.github.tomakehurst" % "wiremock" % "2.27.2" % "it,test"
)
libraryDependencies += "org.jsoup" % "jsoup" % "1.13.1"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.3.1"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1"
libraryDependencies += "io.get-coursier" %% "coursier-core" % "2.0.9"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.9.1"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"
libraryDependencies += "eu.timepit" %% "refined" % "0.9.20"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-core" % "2.1.0"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-macro" % "2.1.0"
libraryDependencies += "com.47deg" %% "github4s" % "0.27.1"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "3.0.0"
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % "0.21.16"
libraryDependencies += "org.scalameta" %% "scalameta" % "4.4.7"
libraryDependencies += "io.get-coursier" %% "coursier" % "2.0.9"
libraryDependencies += "io.get-coursier" %% "coursier-cats-interop" % "2.0.9"
