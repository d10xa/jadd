package ru.d10xa.jadd

class SbtFileAppender {
  def append(fileLines: List[String], dependencies: List[String]): List[String] = {
    fileLines ++ dependencies
  }
}
