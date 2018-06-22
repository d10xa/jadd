package ru.d10xa.jadd.testkit

abstract class BuildFileTestBase(buildFileName: String) {
  private val projectDir = new TempDir()
  private val file: TempFile = projectDir.file(buildFileName)
  val projectPath: String = projectDir.absolutePath
  val projectDirArg: String = s"--project-dir=$projectPath"
  def read(): String = file.read()
  def write(content: String): Unit = file.write(content)
}
