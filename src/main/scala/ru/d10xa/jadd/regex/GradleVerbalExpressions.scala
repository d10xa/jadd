package ru.d10xa.jadd.regex

import ru.lanwen.verbalregex.VerbalExpression

object GradleVerbalExpressions {

  val spaceOrTab: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf(" ", "\t")

  val spaceOrTabOrOpenParenthesis: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf(" ", "\t", "\\(")

  val quote: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf("'", "\"")

  val validNameWithPlaceholders: VerbalExpression.Builder = VerbalExpression
    .regex()
    .add("(?:[\\w-_0-9\\.\\$\\{\\}]+)")

  val variableAssignment: VerbalExpression.Builder = VerbalExpression
    .regex()
    .capt()
    .add(validNameWithPlaceholders)
    .endCapt()
    .add(spaceOrTab).zeroOrMore()
    .`then`("=")
    .add(spaceOrTab).zeroOrMore()
    .add(quote)
    .capt()
    .anything()
    .endCapt()
    .add(quote)

  val validVariableNameRegex: String = "[a-zA-Z_$][a-zA-Z_$0-9]*"

  def stringWithGroupIdArtifactIdVersion(
    configurations: Seq[String] = Seq("compile", "testCompile")
  ): VerbalExpression = {

    VerbalExpression.regex()
      .add(spaceOrTab).zeroOrMore()
      .oneOf(configurations: _*)
      .add(spaceOrTabOrOpenParenthesis).oneOrMore()
      .add(quote)
      .capt().add(validNameWithPlaceholders).endCapt()
      .`then`(":")
      .capt().add(validNameWithPlaceholders).endCapt()
      .`then`(":")
      .capt().add(validNameWithPlaceholders).endCapt()
      .add(quote)
      .build()
  }

  def stringWithGroupIdArtifactId(
    configurations: Seq[String] = Seq("compile", "testCompile")
  ): VerbalExpression = {

    VerbalExpression.regex()
      .add(spaceOrTab).zeroOrMore()
      .oneOf(configurations: _*)
      .add(spaceOrTabOrOpenParenthesis).oneOrMore()
      .add(quote)
      .capt().add(validNameWithPlaceholders).endCapt()
      .`then`(":")
      .capt().add(validNameWithPlaceholders).endCapt()
      .add(quote)
      .build()
  }
}