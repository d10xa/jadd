package ru.d10xa.jadd.inserts

class SbtFileInserts {
  def append(buildFileSource: String, dependencies: Seq[String]): Seq[String] = {
    buildFileSource.split('\n') ++ dependencies
  }
}
