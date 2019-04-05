package ru.d10xa.jadd.regex

import ru.lanwen.verbalregex.VerbalExpression
import ru.d10xa.jadd.regex.CommonVerbalExpressions._

object GradleVerbalExpressions {

  val gradleAllowedConfigurations: List[String] = List(
    "api",
    "implementation",
    "compileOnly",
    "runtimeOnly",
    "testImplementation",
    "testCompileOnly",
    "testRuntimeOnly",
    "compile",
    "testCompile",
    "runtime",
    "testRuntime"
  )

  val spaceOrTabOrOpenParenthesis: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf(" ", "\t", "\\(")

  val validNameWithPlaceholders: VerbalExpression.Builder = VerbalExpression
    .regex()
    .add("(?:[\\w-_0-9\\.\\$\\{\\}]+)")

  val variableAssignment: VerbalExpression.Builder = VerbalExpression
    .regex()
    .capt()
    .add(validNameWithPlaceholders)
    .endCapt()
    .add(spaceOrTab)
    .zeroOrMore()
    .`then`("=")
    .add(spaceOrTab)
    .zeroOrMore()
    .add(anyQuote)
    .capt()
    .anything()
    .endCapt()
    .add(anyQuote)

  val validVariableNameRegex: String = "[a-zA-Z_$][a-zA-Z_$0-9]*"

  def stringWithGroupIdArtifactIdVersion(
    configurations: List[String] = gradleAllowedConfigurations
  ): VerbalExpression =
    VerbalExpression
      .regex()
      .add(spaceOrTab)
      .zeroOrMore()
      .oneOf(configurations: _*)
      .add(spaceOrTabOrOpenParenthesis)
      .oneOrMore()
      .add(anyQuote)
      .capt()
      .add(validNameWithPlaceholders)
      .endCapt()
      .`then`(":")
      .capt()
      .add(validNameWithPlaceholders)
      .endCapt()
      .`then`(":")
      .capt()
      .add(validNameWithPlaceholders)
      .endCapt()
      .add(anyQuote)
      .build()

  def stringWithGroupIdArtifactId(
    configurations: List[String] = gradleAllowedConfigurations
  ): VerbalExpression =
    VerbalExpression
      .regex()
      .add(spaceOrTab)
      .zeroOrMore()
      .oneOf(configurations: _*)
      .add(spaceOrTabOrOpenParenthesis)
      .oneOrMore()
      .add(anyQuote)
      .capt()
      .add(validNameWithPlaceholders)
      .endCapt()
      .`then`(":")
      .capt()
      .add(validNameWithPlaceholders)
      .endCapt()
      .add(anyQuote)
      .build()
}
