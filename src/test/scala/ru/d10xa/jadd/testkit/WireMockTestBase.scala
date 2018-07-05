package ru.d10xa.jadd.testkit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

trait WireMockTestBase extends FunSuiteLike with Matchers with BeforeAndAfterAll {

  val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .usingFilesUnderDirectory("src/test/resources/wiremock/jcenter")
      .port(0)
  )

  def mockedRepositoryUrl = s"http://localhost:${wireMockServer.port()}"

  override protected def beforeAll(): Unit = {
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
  }

}
