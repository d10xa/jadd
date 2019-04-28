package ru.d10xa.jadd.testkit

import better.files._

class TempDir {

  private lazy val tempDir: File =
    File.newTemporaryDirectory("jadd-test-temp-dir").deleteOnExit()

  def file(name: String): TempFile = new TempFile(tempDir, name)

  def absolutePath: String = tempDir.canonicalPath

}
