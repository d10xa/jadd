package ru.d10xa.jadd.testkit

import java.io.File
import java.nio.file.Files

class TempDir {

  private lazy val tempDir: File = {
    val dir = Files.createTempDirectory("jadd-test-temp-dir").toFile
    dir.deleteOnExit()
    dir
  }

  def file(name: String): TempFile = new TempFile(tempDir, name)

  def absolutePath: String = tempDir.getAbsolutePath

}
