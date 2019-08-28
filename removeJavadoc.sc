import $ivy.`com.github.pathikrit:better-files_2.12:3.8.0`
import better.files._
val pathToFiles: String => List[File] = File(_).list(_.isRegularFile).toList
val removeJavadoc: File => Unit =
  f => f.write(f.contentAsString.replaceAll("/\\*\\*(?s:(?!\\*/).)*\\*/", ""))
@main
def main(dir: String): Unit =
  pathToFiles(dir).foreach(removeJavadoc)