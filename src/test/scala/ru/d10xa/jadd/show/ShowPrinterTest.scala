package ru.d10xa.jadd.show

import ru.d10xa.jadd.testkit.TestBase
import scala.reflect.runtime.universe

class ShowPrinterTest extends TestBase {

  test("all printerNames converts to ShowPrinter") {
    ShowPrinter.printerNames
      .map(ShowPrinter.fromString)
      .forall(_.isDefined) shouldBe true
  }

  test("all sealed implementation names defined") {
    val implementationsCount = universe
      .typeOf[ShowPrinter]
      .typeSymbol
      .asClass
      .knownDirectSubclasses
      .size + 2 // gradle +1 implementation, jadd +1 implementation
    implementationsCount shouldEqual ShowPrinter.printerNames.size
  }

}
