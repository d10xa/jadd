package ru.d10xa.jadd
import java.io.File

import org.scalatest.FunSuite
import org.scalatest.Matchers

import scala.io.Source

class ProjectFileReaderMemoryTest extends FunSuite with Matchers {

  val r = new ProjectFileReaderMemory(
    Map(
      "directory/filename" -> "content"
    ))

  test("testRead") {
    val content = r.read("directory/filename").unsafeRunSync()
    content shouldEqual "content"
  }

  test("testRead non-existent file") {
    assertThrows[RuntimeException] {
      r.read("nonexistentfile").unsafeRunSync()
    }
  }

  test("testFile") {
    val file: File = r.file("directory/filename").unsafeRunSync()
    Source.fromFile(file).mkString shouldEqual "content"
  }

  test("testExists") {
    val exists = r.exists("directory/filename").unsafeRunSync()
    exists shouldBe true
  }

  test("testExists non-existent file") {
    val exists = r.exists("nonexistentfile").unsafeRunSync()
    exists shouldBe false
  }

}
