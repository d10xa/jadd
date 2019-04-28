package ru.d10xa.jadd

import better.files._

class SafeFileWriter {

  private val canWriteTo = Set("build.gradle", "build.sbt", "pom.xml")

  def write(file: File, content: String): Unit = {
    require(canWriteTo.contains(file.name))
    file.write(content)
  }

}
