package ru.d10xa.jadd.cli

import ru.d10xa.jadd.cli.Command.Analyze
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.testkit.TestBase

class CliTest extends TestBase {

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

  test("parse --repository") {
    val config = parse(
      Array(
        "install",
        "--repository",
        "https://jcenter.bintray.com,https://repo1.maven.org/maven2,google"
      ))

    config.command shouldEqual Install
    config.repositories shouldEqual Seq(
      "https://jcenter.bintray.com",
      "https://repo1.maven.org/maven2",
      "google"
    )
  }

  test("parse show") {
    parse(Array("show")).command shouldEqual Show
  }

  test("parse i") {
    parse(Array("i")).command shouldEqual Install
  }

  test("parse search") {
    parse(Array("search")).command shouldEqual Search
  }

  test("parse s") {
    parse(Array("s")).command shouldEqual Search
  }

  test("parse --debug") {
    parse(Array("install")).debug.shouldBe(false)
    parse(Array("install", "--debug")).debug.shouldBe(true)
  }

  test("parse --dry-run") {
    parse(Array("install")).dryRun.shouldBe(false)
    parse(Array("install", "--dry-run")).dryRun.shouldBe(true)
  }

  test("parse --project-dir") {
    parse(Array("install", "--project-dir=/tmp")).projectDir.shouldBe("/tmp")
    parse(Array("install", "--project-dir", "/tmp")).projectDir.shouldBe("/tmp")
    parse(Array("--project-dir", "/tmp")).projectDir.shouldBe("/tmp")
  }

  test("parse --shortcuts-uri") {
    val config = parse(
      Array("install", "junit", "--shortcuts-uri=classpath:jadd-shortcuts.csv"))
    config.shortcutsUri shouldEqual "classpath:jadd-shortcuts.csv"
    config.artifacts shouldEqual Seq("junit")
  }

  test("parse fail") {
    parse(Array("42")).command.shouldBe(Help)
    parse(Array("--unknown-argument")).command.shouldBe(Help)
    parse(Array("install", "--unknown-argument")).command.shouldBe(Help)
  }

}
