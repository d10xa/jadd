package ru.d10xa.jadd

import ru.d10xa.jadd.cli.Cli
import ru.d10xa.jadd.log.LoggingUtil
import ru.d10xa.jadd.run.CommandExecutorImpl
import ru.d10xa.jadd.run.JaddRunner
import ru.d10xa.jadd.run.RunParams

object Jadd {

  def main(args: Array[String]): Unit = {
    val runner = new JaddRunner(
      cli = Cli,
      loggingUtil = LoggingUtil
    )
    runner.run(RunParams(args.toVector, new CommandExecutorImpl))
  }

}
