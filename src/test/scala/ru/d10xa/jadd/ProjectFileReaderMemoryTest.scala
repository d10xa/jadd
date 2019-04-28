package ru.d10xa.jadd

import ru.d10xa.jadd.testkit.TestBase

class ProjectFileReaderMemoryTest extends TestBase {

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
    val str =
      r.file("directory/filename").map(_.contentAsString).unsafeRunSync()
    str shouldEqual "content"
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
