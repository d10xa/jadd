package ru.d10xa.jadd.testkit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterAll
import cats.implicits._

/** To record new mappings run wiremock proxy and uncomment mockedRepositoryUrl
  * with port 9999 coursier launch --contrib wiremock -- --port 9999
  * --record-mappings --proxy-all="https://repo1.maven.org/maven2"
  * --root-dir=src/test/resources/wiremock/mavenCentral
  */
abstract class WireMockTestBase extends TestBase with BeforeAndAfterAll {

  val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .usingFilesUnderClasspath("wiremock/mavenCentral")
      .port(0)
  )

  def mockedRepositoryUrl: String =
    s"http://localhost:${wireMockServer.port().show}"
  // Uncomment to record new mappings
  //  def mockedRepositoryUrl = s"http://localhost:9999"

  override protected def beforeAll(): Unit =
    wireMockServer.start()

  override protected def afterAll(): Unit =
    wireMockServer.stop()

}
