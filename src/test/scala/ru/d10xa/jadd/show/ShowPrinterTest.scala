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
      .size
    implementationsCount shouldEqual ShowPrinter.printerNames.size - 1 // gradle has 2 implementations.
  }

}
