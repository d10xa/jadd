name := "jadd"
organization in ThisBuild := "ru.d10xa"

scalaVersion in ThisBuild := "2.13.1"
mainClass in Compile := Some("ru.d10xa.jadd.Jadd")
licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))
description := "Command-line tool for adding dependencies to gradle/maven/sbt build files"

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(
  GitHubHosting("d10xa", "jadd", "Andrey Stolyarov", "d10xa@mail.ru"))

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
      "-Xfatal-warnings", // turn compiler warnings into errors,
      "-Ymacro-annotations" // for @newtype
    )
  )

addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full))

//wartremoverErrors ++= Warts.unsafe
wartremoverErrors in (Compile, compile) ++= Seq(
//  Wart.Any,
  Wart.AnyVal,
  Wart.ArrayEquals,
  Wart.AsInstanceOf,
  Wart.FinalCaseClass,
  Wart.FinalVal,
  Wart.ImplicitConversion,
  Wart.ImplicitParameter,
  Wart.JavaConversions,
  Wart.LeakingSealed,
  Wart.Null,
  Wart.Option2Iterable,
  Wart.Overloading,
  Wart.Product,
  Wart.PublicInference,
  Wart.Return,
  Wart.Serializable,
  Wart.StringPlusAny,
  Wart.ToString,
  Wart.StringPlusAny,
  Wart.TryPartial
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "com.github.scopt" %% "scopt" % "3.7.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.jline" % "jline" % "3.14.1",
  "com.lihaoyi" %% "ujson" % "1.1.0",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.6",
  "org.scalatest" %% "scalatest" % "3.1.1" % "it,test",
  "com.github.tomakehurst" % "wiremock" % "2.26.3" % "it,test"
)
libraryDependencies += "org.jsoup" % "jsoup" % "1.13.1"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.2"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
libraryDependencies += "io.get-coursier" %% "coursier-core" % "2.0.0-RC6-13"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.8"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.3"
libraryDependencies += "eu.timepit" %% "refined" % "0.9.14"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-core" % "2.0.4"
libraryDependencies += "com.github.julien-truffaut" %% "monocle-macro" % "2.0.4"
libraryDependencies += "com.47deg" %% "github4s" % "0.23.0"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "2.2.0"
