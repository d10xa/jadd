package ru.d10xa.jadd.cli

sealed trait Command

object Command {
  case object Install extends Command
  case object Search extends Command
  case object Show extends Command
  case object Analyze extends Command
  case object Help extends Command
  case object Repl extends Command
}
