package ru.d10xa.jadd.cli

import ru.d10xa.jadd.cli.Command.Analyze
import ru.d10xa.jadd.cli.Command.Help
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.show.GradleLang.Kotlin
import ru.d10xa.jadd.show.AmmoniteFormatShowPrinter
import ru.d10xa.jadd.show.GradleFormatShowPrinter
import ru.d10xa.jadd.show.MavenFormatShowPrinter
import ru.d10xa.jadd.testkit.TestBase

class CliTest extends TestBase {

  val cli: Cli = Cli

  def parse(strs: String*): Config = cli.parse(strs.toArray)

  test("parse install") {
    val config = parse("install", "junit", "testng")

    config.command shouldEqual Install
    config.artifacts shouldEqual Seq("junit", "testng")
  }

  test("parse analyze") {
    val config = parse("analyze", "junit:junit")

    config.command shouldEqual Analyze
    config.artifacts shouldEqual Seq("junit:junit")
  }

  test("parse help") {
    val config = parse("help")

    config.command shouldEqual Help
    config.artifacts should be(empty)
  }

  test("parse --repository") {
    val config = parse(
      "install",
      "--repository",
      "https://jcenter.bintray.com,https://repo1.maven.org/maven2,google"
    )
    config.command shouldEqual Install
    config.repositories shouldEqual Seq(
      "https://jcenter.bintray.com",
      "https://repo1.maven.org/maven2",
      "google"
    )
  }

  test("parse show") {
    parse("show").command shouldEqual Show
  }

  test("parse i") {
    parse("i").command shouldEqual Install
  }

  test("parse search") {
    parse("search").command shouldEqual Search
  }

  test("parse s") {
    parse("s").command shouldEqual Search
  }

  test("parse --debug") {
    parse("install").debug.shouldBe(false)
    parse("install", "--debug").debug.shouldBe(true)
  }

  test("parse --dry-run") {
    parse("install").dryRun.shouldBe(false)
    parse("install", "--dry-run").dryRun.shouldBe(true)
  }

  test("parse --project-dir") {
    parse("install", "--project-dir=/tmp").projectDir.shouldBe("/tmp")
    parse("install", "--project-dir", "/tmp").projectDir.shouldBe("/tmp")
    parse("--project-dir", "/tmp").projectDir.shouldBe("/tmp")
  }

  test("parse --output-format") {
    parse("i", "junit", "-f", "amm").showPrinter shouldBe AmmoniteFormatShowPrinter
    parse("i", "junit", "--output-format", "mvn").showPrinter shouldBe MavenFormatShowPrinter
    parse("i", "junit", "--output-format", "maven").showPrinter shouldBe MavenFormatShowPrinter
    parse("i", "--output-format", "maven", "junit").showPrinter shouldBe MavenFormatShowPrinter
  }

  test("parse --shortcuts-uri") {
    val config = parse(
      "install",
      "junit",
      "--shortcuts-uri=classpath:jadd-shortcuts.csv"
    )
    config.shortcutsUri shouldEqual "classpath:jadd-shortcuts.csv"
    config.artifacts shouldEqual Seq("junit")
  }

  test("parse fail") {
    parse("42").command.shouldBe(Help)
    parse("--unknown-argument").command.shouldBe(Help)
    parse("install", "--unknown-argument").command.shouldBe(Help)
  }

}
