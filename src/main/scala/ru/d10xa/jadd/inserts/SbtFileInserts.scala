package ru.d10xa.jadd.inserts

class SbtFileInserts {
  def append(fileLines: List[String], dependencies: List[String]): List[String] = {
    fileLines ++ dependencies
  }
}
