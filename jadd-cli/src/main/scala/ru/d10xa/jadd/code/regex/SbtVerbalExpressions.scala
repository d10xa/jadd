package ru.d10xa.jadd.code.regex

import ru.lanwen.verbalregex.VerbalExpression

object SbtVerbalExpressions {

  val singleLibraryDependency: VerbalExpression =
    VerbalExpression
      .regex()
      .`then`("libraryDependencies")
      .add("\\s")
      .zeroOrMore()
      .`then`("+=")
      .add("\\s")
      .zeroOrMore()
      .capt()
      .anything()
      .endCapt()
      .endOfLine()
      .build()

  val declaredDependency: VerbalExpression =
    VerbalExpression
      .regex()
      .add("\"")
      .capt()
      .anything()
      .endCapt()
      .add("\"")
      .space()
      .zeroOrMore()
      .capt()
      .add("%{1,2}")
      .endCapt()
      .space()
      .zeroOrMore()
      .add("\"")
      .capt()
      .anything()
      .endCapt()
      .add("\"")
      .add("\\s?%\\s?")
      .add("\"")
      .capt()
      .anything()
      .endCapt()
      .add("\"")
      .build()

}
