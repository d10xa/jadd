package ru.d10xa.jadd.regex

import ru.lanwen.verbalregex.VerbalExpression

object GradleVerbalExpressions {

  val spaceOrTab: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf(" ", "\t")

  val quote: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf("'", "\"")

  val validName: VerbalExpression.Builder = VerbalExpression
    .regex()
    .anything()
    .oneOrMore()

  def stringWithGroupIdArtifactIdVersion(
    configurations: Seq[String] = Seq("compile", "testCompile")
  ): VerbalExpression = {

    VerbalExpression.regex()
      .add(spaceOrTab).zeroOrMore()
      .oneOf(configurations: _*)
      .add(spaceOrTab).oneOrMore()
      .add(quote)
      .capt().add(validName).endCapt()
      .`then`(":")
      .capt().add(validName).endCapt()
      .`then`(":")
      .capt().add(validName).endCapt()
      .add(quote)
      .build()

  }
}
