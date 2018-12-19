import Dependencies._

import scala.io.Source

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ru.d10xa",
      scalaVersion := "2.12.6",
      version      := Source.fromFile("VERSION").mkString.trim,
      mainClass in Compile := Some("ru.d10xa.jadd.Jadd")
    )),
    name := "jadd"
  )

scalacOptions ++= Seq(
  "-encoding", "UTF-8",   // source files are in UTF-8
  "-deprecation",         // warn about use of deprecated APIs
  "-unchecked",           // warn about unchecked type parameters
  "-feature",             // warn about misused language features
  "-language:higherKinds",// allow higher kinded types without `import scala.language.higherKinds`
  "-Xlint",               // enable handy linter warnings
  "-Xfatal-warnings",     // turn compiler warnings into errors
  "-Ypartial-unification" // allow the compiler to unify type constructors of different arities
)

enablePlugins(JavaAppPackaging)

coverageExcludedPackages := ".*\\.generated\\..*"

libraryDependencies ++= Seq(
  scalaXml,
  scopt,
  scalaTest % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.jline" % "jline" % "3.7.1",
  "com.lihaoyi" %% "ujson" % "0.6.6",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.5",
  "org.antlr" % "antlr4-runtime" % "4.7.1",
  "com.github.tomakehurst" % "wiremock" % "2.18.0" % Test
)
