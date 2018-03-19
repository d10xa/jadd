package ru.d10xa.jadd.pipelines

trait Pipeline {
  def applicable: Boolean
  def run(): Unit
}
