package ru.d10xa.jadd.testkit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterAll

/**
  * To record new mappings run wiremock proxy and uncomment mockedRepositoryUrl with port 9999
  * wiremock --port 9999 --record-mappings --proxy-all="http://jcenter.bintray.com" --root-dir=src/test/resources/wiremock/jcenter
  */
trait WireMockTestBase extends TestBase with BeforeAndAfterAll {

  val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .usingFilesUnderDirectory("src/test/resources/wiremock/jcenter")
      .port(0)
  )

  def mockedRepositoryUrl = s"http://localhost:${wireMockServer.port()}"
  // Uncomment to record new mappings
  //  def mockedRepositoryUrl = s"http://localhost:9999"

  override protected def beforeAll(): Unit =
    wireMockServer.start()

  override protected def afterAll(): Unit =
    wireMockServer.stop()

}
