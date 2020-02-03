name := "jadd"
organization in ThisBuild := "ru.d10xa"

scalaVersion in ThisBuild := "2.13.1"
version in ThisBuild := IO.read(new File("VERSION")).trim
mainClass in Compile := Some("ru.d10xa.jadd.Jadd")

lazy val root = project
  .in(file("."))
  .settings(
    scalacOptions := Seq(
      "-encoding",
      "UTF-8", // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked", // warn about unchecked type parameters
      "-feature", // warn about misused language features
      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
      "-Xlint", // enable handy linter warnings
      "-Xfatal-warnings" // turn compiler warnings into errors
    )
  )

enablePlugins(JavaAppPackaging)
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
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.github.scopt" %% "scopt" % "3.7.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.jline" % "jline" % "3.13.3",
  "com.lihaoyi" %% "ujson" % "0.9.8",
  "ru.lanwen.verbalregex" % "java-verbal-expressions" % "1.6",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.26.0" % Test
)
libraryDependencies += "org.jsoup" % "jsoup" % "1.12.1"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.0"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
libraryDependencies += "io.get-coursier" %% "coursier-core" % "2.0.0-RC6"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.8"
