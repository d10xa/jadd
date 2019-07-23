package ru.d10xa.jadd

final case class RunParams(
  args: Vector[String],
  commandExecutor: CommandExecutor
)
