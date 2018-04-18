package ru.d10xa.jadd.inserts

class GradleFileInserts {
  def append(buildFileSource: String, dependencies: Seq[String]): Seq[String] = {
    val fileLines = buildFileSource.split('\n')

    val index = fileLines indexWhere (_.startsWith("dependencies {"))
    val (a, b) = fileLines splitAt index + 1
    val indent =
      if (b.head.startsWith("}")) "    "
      else b.head.takeWhile(c => c == ' ' || c == '\t')
    a ++ dependencies.map(indent + _) ++ b
  }
}
