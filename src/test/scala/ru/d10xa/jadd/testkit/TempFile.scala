package ru.d10xa.jadd.testkit

import java.io.File
import java.nio.file.Files

class TempFile(dir: File, name: String) {
  lazy val file: File = {
    val f = new File(dir, name)
    if (!f.exists()) { f.deleteOnExit() }
    f
  }
  def read(): String = new String(Files.readAllBytes(file.toPath))
  def write(content: String): Unit = {
    Files.write(file.toPath, content.getBytes())
  }
  def absolutePath: String = file.getAbsolutePath
}
