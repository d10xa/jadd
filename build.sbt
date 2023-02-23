name := "jadd"
ThisBuild / organization := "ru.d10xa"

ThisBuild / scalaVersion := "2.13.10"
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
      "-Xlint" // enable handy linter warnings
//      "-Xfatal-warnings", // turn compiler warnings into errors,
    ),
    scalacOptions ++= (if (scalaVersion.value.startsWith("3")) Seq("-explain-types", "-Ykind-projector")
    else Seq("-explaintypes", "-Wunused"))
  )

libraryDependencies ++= {
  scalaVersion.value match {
    case s if s.startsWith("3") =>
      Nil
    case _ =>
      List(compilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full))
  }
}

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.typelevel" %% "cats-core" % "2.9.0",
  "org.jline" % "jline" % "3.21.0",
  "com.lihaoyi" %% "ujson" % "2.0.0",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.8",
  "org.scalatest" %% "scalatest" % "3.2.14" % "it,test",
  "com.github.tomakehurst" % "wiremock" % "2.27.2" % "it,test"
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5"
libraryDependencies += "org.jsoup" % "jsoup" % "1.15.3"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.8"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1" cross CrossVersion.for3Use2_13
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.11.1"
libraryDependencies += "eu.timepit" %% "refined" % "0.10.1"
libraryDependencies += "dev.optics" %% "monocle-core" % "3.1.0"
libraryDependencies += "dev.optics" %% "monocle-macro" % "3.1.0"
libraryDependencies += "com.47deg" %% "github4s" % "0.31.2"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "4.0.3"
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % "0.23.13"
libraryDependencies += "org.scalameta" %% "scalameta" % "4.7.0" cross CrossVersion.for3Use2_13
libraryDependencies += "io.get-coursier" %% "coursier" % "2.0.16" cross CrossVersion.for3Use2_13
libraryDependencies += "io.get-coursier" %% "coursier-core" % "2.0.16" cross CrossVersion.for3Use2_13
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.8.1"
