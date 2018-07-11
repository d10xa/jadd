package ru.d10xa.jadd.regex

import ru.lanwen.verbalregex.VerbalExpression

// TODO add optional parenthesis like `compile("org.springframework.boot:spring-boot-starter-web")`
object GradleVerbalExpressions {

  val spaceOrTab: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf(" ", "\t")

  val quote: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf("'", "\"")

  val validName: VerbalExpression.Builder = VerbalExpression
    .regex()
    .add("(?:[\\w-_0-9\\.]+)")

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

  def stringWithGroupIdArtifactId(
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
      .add(quote)
      .build()
  }
}
