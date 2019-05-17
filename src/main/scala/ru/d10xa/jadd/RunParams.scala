package ru.d10xa.jadd

final case class RunParams(
  args: Array[String],
  commandExecutor: CommandExecutor
)
