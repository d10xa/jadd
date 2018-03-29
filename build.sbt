import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ru.d10xa",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT",
      mainClass in Compile := Some("ru.d10xa.jadd.Main")
    )),
    name := "jadd"
  )

assemblyJarName in assembly := "jadd.jar"

libraryDependencies += scalaXml
libraryDependencies += scopt
libraryDependencies += scalaTest % Test
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.8.0"

enablePlugins(JavaAppPackaging)
