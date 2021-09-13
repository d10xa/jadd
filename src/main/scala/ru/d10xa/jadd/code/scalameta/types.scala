package ru.d10xa.jadd.code.scalameta

object types {

  type WithCounter[A] = (Int, A)

  type ValuesMap = Map[Vector[String], VariableLitP]

}
