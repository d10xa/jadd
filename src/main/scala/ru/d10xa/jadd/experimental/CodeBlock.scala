package ru.d10xa.jadd.experimental

object CodeBlock {

  val braces = Map(
    '{' -> '}',
    '(' -> ')',
  )

  def extractBlockContent(sourceLeftTrimmed: String, openChar: Char, closeChar: Char): Option[String] = {
    var open = 1
    var inComment = false
    var slash = false
    val r = sourceLeftTrimmed.takeWhile {
      c =>
        val inc = if (c == openChar && !inComment) 1
        else if (c == closeChar && !inComment) -1
        else 0
        open = open + inc
        if(slash) inComment = true
        if(c == '/') slash = true
        if(c == '\n') inComment = false
        if (open == 0) false
        else true
    }
    if(open == 0) Some(r) else None
  }

  /**
   *
   * @param source build file source
   * @param block block with leading open brace e.x dependencies {
   * @return substring dependencies { ... }
   */
  def extractBlockContent(source: String, block: String): Option[String] = {
    val index: Int = source.indexOf(block)
    val openChar: Option[Char] = block.lastOption
    val closeChar: Option[Char] = openChar.flatMap(braces.get)
    def sourceLeftTrimmed:String = source.substring(index).drop(block.length)
    if (index == -1) None
    else {
      for {
        op <- openChar
        cl <- closeChar
        res <- extractBlockContent(sourceLeftTrimmed, op, cl)
      } yield res
    }
  }

}
