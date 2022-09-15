name := "jadd"
ThisBuild / organization := "ru.d10xa"

ThisBuild / scalaVersion := "2.13.8"
Compile / mainClass := Some("ru.d10xa.jadd.Jadd")
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
  ("org.typelevel" %% "kind-projector" % "0.13.2").cross(CrossVersion.full)
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.typelevel" %% "cats-core" % "2.8.0",
  "org.jline" % "jline" % "3.21.0",
  "com.lihaoyi" %% "ujson" % "2.0.0",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.8",
  "org.scalatest" %% "scalatest" % "3.2.13" % "it,test",
  "com.github.tomakehurst" % "wiremock" % "2.27.2" % "it,test"
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11"
libraryDependencies += "org.jsoup" % "jsoup" % "1.15.2"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.14"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.11.1"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"
libraryDependencies += "eu.timepit" %% "refined" % "0.10.1"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-core" % "2.1.0"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-macro" % "2.1.0"
libraryDependencies += "com.47deg" %% "github4s" % "0.31.2"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "4.0.2"
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % "0.23.12"
libraryDependencies += "org.scalameta" %% "scalameta" % "4.5.13"
libraryDependencies += "io.get-coursier" %% "coursier" % "2.0.16"
libraryDependencies += "io.get-coursier" %% "coursier-core" % "2.0.16"
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.7.3"
