package ru.d10xa.jadd.core

import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.URI

final case class ProxySettings(
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

  val httpProxyHost: String = "http.proxyHost"
  val httpsProxyHost: String = "https.proxyHost"
  val httpProxyPort: String = "http.proxyPort"
  val httpsProxyPort: String = "https.proxyPort"
  val httpProxyUser: String = "http.proxyUser"
  val httpsProxyUser: String = "https.proxyUser"
  val httpProxyPassword: String = "http.proxyPassword"
  val httpsProxyPassword: String = "https.proxyPassword"
  val tunnelingDisabledSchemes: String =
    "jdk.http.auth.tunneling.disabledSchemes"
  val proxyingDisabledSchemes: String = "jdk.http.auth.proxying.disabledSchemes"

  def fromURI(proxy: URI): ProxySettings = {

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

  private def optProp(key: String): Option[String] =
    Option(System.getProperty(key))

  private def propSet(key: String, value: Option[String]): Unit = value match {
    case Some(v) => System.setProperty(key, v)
    case None => System.clearProperty(key)
  }

  def get(): ProxySettings =
    ProxySettings(
      httpHost = optProp(httpProxyHost),
      httpsHost = optProp(httpsProxyHost),
      httpPort = optProp(httpProxyPort),
      httpsPort = optProp(httpsProxyPort),
      httpProxyUser = optProp(httpProxyUser),
      httpsProxyUser = optProp(httpsProxyUser),
      httpProxyPassword = optProp(httpProxyPassword),
      httpsProxyPassword = optProp(httpsProxyPassword),
      tunnelingDisabledSchemes = optProp(tunnelingDisabledSchemes),
      proxyingDisabledSchemes = optProp(proxyingDisabledSchemes)
    )

  def set(proxySettings: ProxySettings): Unit = {
    propSet(httpProxyHost, proxySettings.httpHost)
    propSet(httpsProxyHost, proxySettings.httpsHost)
    propSet(httpProxyPort, proxySettings.httpPort)
    propSet(httpsProxyPort, proxySettings.httpsPort)
    propSet(httpProxyUser, proxySettings.httpProxyUser)
    propSet(httpsProxyUser, proxySettings.httpsProxyUser)
    propSet(httpProxyPassword, proxySettings.httpProxyPassword)
    propSet(httpsProxyPassword, proxySettings.httpsProxyPassword)
    propSet(tunnelingDisabledSchemes, proxySettings.tunnelingDisabledSchemes)
    propSet(proxyingDisabledSchemes, proxySettings.proxyingDisabledSchemes)
  }

  def setupAuthenticator(user: String, password: String): Unit = {
    val a = new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(user, password.toCharArray)
    }
    Authenticator.setDefault(a)
  }

}
