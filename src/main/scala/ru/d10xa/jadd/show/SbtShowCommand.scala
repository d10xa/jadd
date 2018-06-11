package ru.d10xa.jadd.show

class SbtShowCommand(buildFileSource: String) {
  def show(): String = {
    val matches = raw"""["'].+["']\s%{1,2}\s["'].+["']\s%\s["'].+["'](\s%\sTest)?""".r.findAllMatchIn(buildFileSource)
    matches.toList.map(_.group(0)).mkString("\n")
  }
}
