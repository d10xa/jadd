name := "jadd"
organization in ThisBuild := "ru.d10xa"
scalaVersion in ThisBuild := "2.12.8"
version in ThisBuild := scala.io.Source.fromFile("VERSION").mkString.trim
mainClass in Compile := Some("ru.d10xa.jadd.Jadd")

lazy val antlrSbtDependencies = project.in(file("antlr-sbt-dependencies"))

lazy val root = project
  .in(file("."))
  .dependsOn(antlrSbtDependencies)
  .settings(
    scalacOptions := Seq(
      "-encoding",
      "UTF-8", // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked", // warn about unchecked type parameters
      "-feature", // warn about misused language features
      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
      "-Xlint", // enable handy linter warnings
      "-Xfatal-warnings", // turn compiler warnings into errors
      "-Ypartial-unification" // allow the compiler to unify type constructors of different arities
    )
  )

enablePlugins(JavaAppPackaging)
//wartremoverErrors ++= Warts.unsafe
wartremoverErrors in (Compile, compile) ++= Seq(
  Wart.FinalCaseClass,
  Wart.ImplicitParameter,
  Wart.JavaConversions,
  Wart.LeakingSealed,
  Wart.Overloading,
  Wart.Product,
  Wart.PublicInference,
  Wart.Serializable,
  Wart.StringPlusAny,
  Wart.ToString
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.jline" % "jline" % "3.7.1",
  "com.lihaoyi" %% "ujson" % "0.6.6",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.5",
  "com.github.tomakehurst" % "wiremock" % "2.18.0" % Test
)
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.2.0"
