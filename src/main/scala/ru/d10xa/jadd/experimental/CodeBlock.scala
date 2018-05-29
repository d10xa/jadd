package ru.d10xa.jadd.experimental

case class CodeBlock(
  outerIndex: Int,
  innerStartIndex: Int,
  innerEndIndex: Int,
  innerContent: String
)

object CodeBlock {

  val braces = Map(
    '{' -> '}',
    '(' -> ')',
  )

  def find(source: String, block: String): Seq[CodeBlock] = {
    val outerIndex: Int = source.indexOf(block)
    val innerIndex: Int = outerIndex + block.length
    val openChar: Option[Char] = block.lastOption
    val closeChar: Option[Char] = openChar.flatMap(braces.get)
    def sourceLeftTrimmed:String = source.substring(innerIndex)
    if (outerIndex == -1) Seq.empty
    else {
      for {
        op <- openChar.toList
        cl <- closeChar.toList
        res <- extractBlockContentFromLeftTrimmed(sourceLeftTrimmed, op, cl)
      } yield CodeBlock(
        outerIndex = outerIndex,
        innerStartIndex = innerIndex,
        innerEndIndex = innerIndex + res.length,
        innerContent = res
      )
    }
  }

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

}
