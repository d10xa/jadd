package ru.d10xa.jadd.code.regex

import ru.lanwen.verbalregex.VerbalExpression

object CommonVerbalExpressions {

  val spaceOrTab: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf(" ", "\t")

  val anyQuote: VerbalExpression.Builder = VerbalExpression
    .regex()
    .oneOf("'", "\"")

}
