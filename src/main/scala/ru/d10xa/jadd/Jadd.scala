package ru.d10xa.jadd

import ru.d10xa.jadd.cli.Cli

object Jadd {

  def main(args: Array[String]): Unit = {
    val runner = new JaddRunner(
      cli = Cli,
      commandExecutor = new CommandExecutorImpl,
      loggingUtil = LoggingUtil
    )
    runner.run(args)
  }

}
