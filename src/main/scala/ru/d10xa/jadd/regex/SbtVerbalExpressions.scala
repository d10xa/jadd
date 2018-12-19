package ru.d10xa.jadd.regex

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

}
