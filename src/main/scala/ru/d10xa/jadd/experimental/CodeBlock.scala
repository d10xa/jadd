package ru.d10xa.jadd.experimental

object CodeBlock {

  val braces = Map(
    '{' -> '}',
    '(' -> ')',
  )

  private def extractBlockContentFromLeftTrimmed(
    sourceLeftTrimmed: String,
    openChar: Char,
    closeChar: Char
  ): Seq[String] = {
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
    if(open == 0) Seq(r) else Seq.empty
  }

  /**
   *
   * @param source build file source
   * @param block block with leading open brace e.x dependencies {
   * @return sequence of matches. (indexOf, string match)
   */
  // TODO Implement sequences with more than 1 elements
  def extractBlockContent(source: String, block: String): Seq[(Int, String)] = {
    val index: Int = source.indexOf(block)
    val openChar: Option[Char] = block.lastOption
    val closeChar: Option[Char] = openChar.flatMap(braces.get)
    def sourceLeftTrimmed:String = source.substring(index).drop(block.length)
    if (index == -1) Seq.empty
    else {
      for {
        op <- openChar.toList
        cl <- closeChar.toList
        res <- extractBlockContentFromLeftTrimmed(sourceLeftTrimmed, op, cl).map(index -> _)
      } yield res
    }
  }

  def findBlockContent(source: String, block: String): Seq[(Int, Int)] =
    extractBlockContent(source, block).map { case (i, s) => (i, i + s.length)}

}
