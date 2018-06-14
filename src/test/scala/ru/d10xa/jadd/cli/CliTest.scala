package ru.d10xa.jadd.cli

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.cli.Command.Analyze
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Install

class CliTest extends FunSuite with Matchers {

  val cli: Cli = Cli

  val parse: Array[String] => Config = cli.parse

  test("parse install") {
    val config = parse(Array("install", "junit", "testng"))

    config.command shouldEqual Install
    config.artifacts shouldEqual Seq("junit", "testng")
  }

  test("parse analyze") {
    val config = parse(Array("analyze", "junit:junit"))

    config.command shouldEqual Analyze
    config.artifacts shouldEqual Seq("junit:junit")
  }

  test("parse help") {
    val config = parse(Array("help"))

    config.command shouldEqual Help
    config.artifacts should be(empty)
  }

}
