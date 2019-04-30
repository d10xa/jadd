package ru.d10xa.jadd.repl

import ru.d10xa.jadd.testkit.TestBase

class JaddJlineCompleterTest extends TestBase {

  import JaddJlineCompleter._

  test("matchOpt true") {
    m("s", "-f", "a") shouldBe true
    m("s", "-f") shouldBe true
    m("s", "-f", "") shouldBe true
    m("s", "x", "-f", "") shouldBe true
  }

  test("matchOpt false") {
    m("s", "-x", "") shouldBe false
    m("-f", "amm", "-o") shouldBe false
    m("--output-format", "amm", "-o") shouldBe false
  }

  def m(strs: String*): Boolean = matchFormatOutput(strs)

}
