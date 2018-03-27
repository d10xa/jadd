package ru.d10xa.jadd.inserts

import org.scalatest.FunSuite
import org.scalatest.Matchers

class MiddleInsertTest extends FunSuite with Matchers {
  test("check insert") {
    val result = MiddleInsert.insert(List("1", "2", "3"), List("0", "-1"), 1)

    result shouldEqual List("1", "0", "-1", "2", "3")
  }

  test("negative") {
    val result = MiddleInsert.insert(List("1", "2", "3", "4"), List("0", "-1"), -1)

    result shouldEqual List("1", "2", "3", "0", "-1", "4")
  }
}
