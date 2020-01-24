package ru.d10xa.jadd.core

import better.files._

class SafeFileWriter {

  private val canWriteTo = Set("build.gradle", "build.sbt", "pom.xml")
  private val supportedExtensions = Set(".sc")

  def write(file: File, content: String): Unit = {
    def extensionSuitable: Boolean =
      supportedExtensions.exists(file.name.endsWith(_))
    require(canWriteTo.contains(file.name) || extensionSuitable)
    file.write(content)
  }

}
