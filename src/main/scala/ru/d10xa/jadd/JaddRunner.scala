package ru.d10xa.jadd

import java.net.URI

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
      if (config.debug) loggingUtil.enableDebug()
      if (config.quiet) loggingUtil.quiet()
      config.proxy.foreach(initProxy)
      config
    }

    def initProxy(proxyStr: String): Unit = {
      val proxyUri = new URI(proxyStr)
      val proxySettings = ProxySettings(proxyUri)
      ProxySettings.set(proxySettings)
      (proxySettings.httpProxyUser, proxySettings.httpProxyPassword) match {
        case (Some(u), Some(p)) => ProxySettings.setupAuthenticator(u, p)
        case _ => // do nothing
      }
    }

    def runOnceForRepl(args: Array[String]): Unit =
      runOnce(args, readAndEvalConfig(args))

    val config: Config = readAndEvalConfig(args)
    if (config.command == Repl) ReplCommand.runRepl(runOnceForRepl)
    else runOnce(args, config)
  }

}
