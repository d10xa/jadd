package ru.d10xa.jadd.inserts

class GradleFileInserts {
  def append(fileLines: List[String], dependencies: List[String]): List[String] = {
    val index = fileLines indexWhere (_.startsWith("dependencies {"))
    val (a, b) = fileLines splitAt index + 1
    val indent =
      if (b.head.startsWith("}")) "    "
      else b.head.takeWhile(c => c == ' ' || c == '\t')
    a ++ dependencies.map(indent + _) ++ b
  }
}
