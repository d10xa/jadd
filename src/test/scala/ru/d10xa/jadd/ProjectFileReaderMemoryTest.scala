package ru.d10xa.jadd

import better.files.File
import cats.effect.IO
import ru.d10xa.jadd.core.ProjectFileReaderMemory
import ru.d10xa.jadd.testkit.TestBase

class ProjectFileReaderMemoryTest extends TestBase {

  val r = new ProjectFileReaderMemory[IO](
    Map(
      "directory/filename" -> "content"
    ))

  def read(s: String): String = r.read(s).unsafeRunSync()
  def file(s: String): File = r.file(s).unsafeRunSync()
  def exists(s: String): Boolean = r.exists(s).unsafeRunSync()

  test("testRead") {
    val content = read("directory/filename")
    content shouldEqual "content"
  }

  test("testRead non-existent file") {
    assertThrows[RuntimeException] {
      read("nonexistentfile")
    }
  }

  test("testFile") {
    val str =
      file("directory/filename").contentAsString
    str shouldEqual "content"
  }

  test("testExists") {
    exists("directory/filename") shouldBe true
  }

  test("testExists non-existent file") {
    exists("nonexistentfile") shouldBe false
  }

}
