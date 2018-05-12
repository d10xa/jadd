import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ru.d10xa",
      scalaVersion := "2.12.5",
      version      := "0.1.6",
      mainClass in Compile := Some("ru.d10xa.jadd.Main")
    )),
    name := "jadd"
  )

assemblyJarName in assembly := "jadd.jar"

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

libraryDependencies += scalaXml
libraryDependencies += scopt
libraryDependencies += scalaTest % Test
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"
libraryDependencies += "io.circe" %% "circe-parser" % "0.9.3"
libraryDependencies += "io.circe" %% "circe-generic" % "0.9.3"
