package ru.d10xa.jadd.run

final case class RunParams[F[_]](
  args: Vector[String]
)
