package ru.d10xa.jadd.code.stringinterpolation

import ru.d10xa.jadd.testkit.TestBase

class GStrInterpolationTest extends TestBase {

  test("GStr placeholders") {
    val gstr = new GStr("!$hello.${world}!!!$123")

    gstr.placeholders() shouldEqual Set("hello", "world")
  }

  test("GStr resolve") {
    val gstr = new GStr("!$hello.${world}!!!$123")

    gstr.resolve(Map("hello" -> "1", "world" -> "2")) shouldEqual "!1.2!!!$123"
  }

  test("interpolation resolver") {
    import GStr.interpolate

    val source = Map(
      "aa" -> "foo",
      "bb" -> "bar",
      "cc" -> "${aa}$bb",
      "dd" -> "$cc${cc}"
    )

    val result = Map(
      "aa" -> "foo",
      "bb" -> "bar",
      "cc" -> "foobar",
      "dd" -> "foobarfoobar"
    )

    interpolate(source) shouldEqual result
  }

  test("partial interpolation") {
    import GStr.interpolate

    val source = Map(
      "aa" -> "foo",
      "bb" -> "bar",
      "cc" -> "${aa}$bb",
      "dd" -> "$cc${cc}$unknown"
    )

    val result = Map(
      "aa" -> "foo",
      "bb" -> "bar",
      "cc" -> "foobar",
      "dd" -> "foobarfoobar$unknown"
    )

    interpolate(source) shouldEqual result
  }

}
