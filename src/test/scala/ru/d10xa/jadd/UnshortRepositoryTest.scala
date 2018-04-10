package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder.unshortRepository

class UnshortRepositoryTest extends FunSuite with Matchers {

  test("unshort bintray"){
    unshortRepository("bintray/groovy/maven") shouldEqual "https://dl.bintray.com/groovy/maven"
  }

  test("as is"){
    unshortRepository("https://dl.bintray.com/groovy/maven") shouldEqual "https://dl.bintray.com/groovy/maven"

  }

}
