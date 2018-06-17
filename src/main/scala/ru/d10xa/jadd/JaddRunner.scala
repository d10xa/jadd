package ru.d10xa.jadd

import com.typesafe.scalalogging.StrictLogging
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.cli.Command.Repl
import ru.d10xa.jadd.cli.Cli

class JaddRunner(
  cli: Cli,
  loggingUtil: LoggingUtil,
  commandExecutor: CommandExecutor
) extends StrictLogging {

  def run(args: Array[String]): Unit = {

    def runOnce(args: Array[String], config: Config): Unit =
      commandExecutor.execute(config, () => logger.info(config.usage))

    def readConfig(args: Array[String]): Config = {
      val config = cli.parse(args)
      if(config.debug) loggingUtil.enableDebug()
      config
    }

    def runOnceForRepl(args: Array[String]): Unit =
      runOnce(args, readConfig(args))

    val config: Config = readConfig(args)
    if (config.command == Repl) ReplCommand.runRepl(runOnceForRepl)
    else runOnce(args, config)
  }

}
