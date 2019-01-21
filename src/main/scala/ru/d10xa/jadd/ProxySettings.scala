package ru.d10xa.jadd

import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.URI

case class ProxySettings(
  httpHost: Option[String],
  httpsHost: Option[String],
  httpPort: Option[String],
  httpsPort: Option[String],
  httpProxyUser: Option[String],
  httpsProxyUser: Option[String],
  httpProxyPassword: Option[String],
  httpsProxyPassword: Option[String],
  tunnelingDisabledSchemes: Option[String],
  proxyingDisabledSchemes: Option[String]
)

object ProxySettings {

  val httpProxyHost = "http.proxyHost"
  val httpsProxyHost = "https.proxyHost"
  val httpProxyPort = "http.proxyPort"
  val httpsProxyPort = "https.proxyPort"
  val httpProxyUser = "http.proxyUser"
  val httpsProxyUser = "https.proxyUser"
  val httpProxyPassword = "http.proxyPassword"
  val httpsProxyPassword = "https.proxyPassword"
  val tunnelingDisabledSchemes = "jdk.http.auth.tunneling.disabledSchemes"
  val proxyingDisabledSchemes = "jdk.http.auth.proxying.disabledSchemes"

  def apply(proxy: URI): ProxySettings = {

    val host = proxy.getHost
    val port = proxy.getPort.toString
    val userAndPassword =
      Option(proxy.getUserInfo)
        .map(_.split(':'))
        .collect { case Array(u, pass) => (u, pass) }


    ProxySettings(
      httpHost = Some(host),
      httpsHost = Some(host),
      httpPort = Some(port),
      httpsPort = Some(port),
      httpProxyUser = userAndPassword.map(_._1),
      httpsProxyUser = userAndPassword.map(_._1),
      httpProxyPassword = userAndPassword.map(_._2),
      httpsProxyPassword = userAndPassword.map(_._2),
      tunnelingDisabledSchemes = Some(""),
      proxyingDisabledSchemes = Some("")
    )

  }

  private def prop(key: String): Option[String] = Option(System.getProperty(key))

  private def prop(key: String, value: Option[String]): Unit = value match {
    case Some(v) => System.setProperty(key, v)
    case None => System.clearProperty(key)
  }

  def get(): ProxySettings = {
    ProxySettings(
      httpHost = prop(httpProxyHost),
      httpsHost = prop(httpsProxyHost),
      httpPort = prop(httpProxyPort),
      httpsPort = prop(httpsProxyPort),
      httpProxyUser = prop(httpProxyUser),
      httpsProxyUser = prop(httpsProxyUser),
      httpProxyPassword = prop(httpProxyPassword),
      httpsProxyPassword = prop(httpsProxyPassword),
      tunnelingDisabledSchemes = prop(tunnelingDisabledSchemes),
      proxyingDisabledSchemes = prop(proxyingDisabledSchemes)
    )
  }

  def set(proxySettings: ProxySettings): Unit = {
    prop(httpProxyHost, proxySettings.httpHost)
    prop(httpsProxyHost, proxySettings.httpsHost)
    prop(httpProxyPort, proxySettings.httpPort)
    prop(httpsProxyPort, proxySettings.httpsPort)
    prop(httpProxyUser, proxySettings.httpProxyUser)
    prop(httpsProxyUser, proxySettings.httpsProxyUser)
    prop(httpProxyPassword, proxySettings.httpProxyPassword)
    prop(httpsProxyPassword, proxySettings.httpsProxyPassword)
    prop(tunnelingDisabledSchemes, proxySettings.tunnelingDisabledSchemes)
    prop(proxyingDisabledSchemes, proxySettings.proxyingDisabledSchemes)
  }

  def setupAuthenticator(user: String, password: String): Unit = {
    val a = new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(user, password.toCharArray)
    }
    Authenticator.setDefault(a)
  }

}
