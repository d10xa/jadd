name := "jadd"
ThisBuild / organization := "ru.d10xa"

ThisBuild / scalaVersion := "2.13.6"
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
  "com.github.scopt" %% "scopt" % "4.0.1",
  "org.typelevel" %% "cats-core" % "2.6.1",
  "org.jline" % "jline" % "3.20.0",
  "com.lihaoyi" %% "ujson" % "1.4.2",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.8",
  "org.scalatest" %% "scalatest" % "3.2.10" % "it,test",
  "com.github.tomakehurst" % "wiremock" % "2.27.2" % "it,test"
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.6"
libraryDependencies += "org.jsoup" % "jsoup" % "1.14.3"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.2.9"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.9.2"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"
libraryDependencies += "eu.timepit" %% "refined" % "0.9.27"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-core" % "2.1.0"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-macro" % "2.1.0"
libraryDependencies += "com.47deg" %% "github4s" % "0.30.0"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "3.6.0"
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % "0.23.6"
libraryDependencies += "org.scalameta" %% "scalameta" % "4.4.29"
libraryDependencies += "io.get-coursier" %% "coursier" % "2.0.16"
libraryDependencies += "io.get-coursier" %% "coursier-core" % "2.0.16"
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.6.6"
