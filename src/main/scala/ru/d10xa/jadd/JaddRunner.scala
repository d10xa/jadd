package ru.d10xa.jadd

import java.net.Authenticator
import java.net.PasswordAuthentication

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Config

class JaddRunner(
  cli: Cli,
  loggingUtil: LoggingUtil,
  commandExecutor: CommandExecutor
) extends StrictLogging {

  def run(args: Array[String]): Unit = {

    def runOnce(args: Array[String], config: Config): Unit =
      commandExecutor.execute(config, () => logger.info(config.usage))

    def readAndEvalConfig(args: Array[String]): Config = {
      val config = cli.parse(args)
      if(config.debug) loggingUtil.enableDebug()
      if(config.quiet) loggingUtil.quiet()
      config.proxy.foreach(initProxy)

      config
    }

    def initProxy(proxyStr: String): Unit = {

      def init(host: String, port: String, auth: Option[(String, String)]): Unit = {
        System.setProperty("http.proxyHost", host)
        System.setProperty("https.proxyHost", host)
        System.setProperty("http.proxyPort", port)
        System.setProperty("https.proxyPort", port)
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "")
        System.setProperty("jdk.http.auth.proxying.disabledSchemes", "")
        auth.foreach {
          case (user, password) =>
            System.setProperty("http.proxyUser", user)
            System.setProperty("https.proxyUser", user)
            System.setProperty("http.proxyPassword", password)
            System.setProperty("https.proxyPassword", password)

            val authenticator = new Authenticator {
              override def getPasswordAuthentication: PasswordAuthentication =
                new PasswordAuthentication(user, password.toCharArray)
            }
            Authenticator.setDefault(authenticator)
        }
      }

      val userPassHostPort = "(.+):(.+)@(.+):(.+)".r
      val hostPort = "(.+):(.+)".r
      val t = proxyStr match {
        case userPassHostPort(user, password, host, port) =>
          (host, port, Some((user, password)))
        case hostPort(host, port) =>
          (host, port, None)
      }
      (init _).tupled(t)
    }

    def runOnceForRepl(args: Array[String]): Unit =
      runOnce(args, readAndEvalConfig(args))

    val config: Config = readAndEvalConfig(args)
    if (config.command == Repl) ReplCommand.runRepl(runOnceForRepl)
    else runOnce(args, config)
  }

}
