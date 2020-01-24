package ru.d10xa.jadd.run

final case class RunParams(
  args: Vector[String],
  commandExecutor: CommandExecutor
)
