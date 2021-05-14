package ru.d10xa.jadd.code.scalameta

import scala.meta.Tree
import scala.meta.inputs.Position
import scala.meta.transversers.SimpleTraverser

object ScalametaUtils {

  /** Modified version of scala.meta.transversers.Api.XtensionCollectionLikeUI#collect(scala.PartialFunction)
    */
  def collectNoDuplicate[T](
    tree: Tree,
    fn: PartialFunction[Tree, T]
  ): List[T] = {
    val liftedFn = fn.lift
    val buf = scala.collection.mutable.ListBuffer[T]()
    object traverser extends SimpleTraverser {
      override def apply(tree: Tree): Unit = {
        val x = liftedFn(tree)
        x.foreach(buf += _)
        if (x.isEmpty) super.apply(tree)
      }
    }
    traverser(tree)
    buf.toList
  }

  def replacePositions(
    source: String,
    replaces: List[(Position, String)]
  ): String =
    replaces
      .foldLeft((new StringBuilder(source), 0)) {
        case ((source, shift), (pos, value)) =>
          source
            .replace(
              pos.start - shift,
              pos.end - shift,
              value
            ) -> (pos.end - pos.start - value.length + shift)
      }
      ._1
      .toString()

}
