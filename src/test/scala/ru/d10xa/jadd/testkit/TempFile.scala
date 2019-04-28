package ru.d10xa.jadd.testkit

import better.files._

class TempFile(dir: File, name: String) {
  lazy val file: File = {
    val f = File(dir, name)
    if (!f.exists()) { f.deleteOnExit() }
    f
  }
  def read(): String = file.contentAsString
  def write(content: String): Unit =
    file.writeByteArray(content.getBytes())
  def absolutePath: String = file.canonicalPath
}
