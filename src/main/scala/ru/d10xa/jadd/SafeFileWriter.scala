package ru.d10xa.jadd

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class SafeFileWriter {
  def write(file: File, content: String): Unit = {
    require(file.getName == "build.gradle")
    val w = new BufferedWriter(new FileWriter(file))
    w.write(content)
    w.close()
  }
}
