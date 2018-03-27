package ru.d10xa.jadd

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class SafeFileWriter {

  private val canWriteTo = Set("build.gradle", "build.sbt", "pom.xml")

  def write(file: File, content: String): Unit = {
    require(canWriteTo.contains(file.getName))
    val w = new BufferedWriter(new FileWriter(file))
    w.write(content)
    w.close()
  }
}
