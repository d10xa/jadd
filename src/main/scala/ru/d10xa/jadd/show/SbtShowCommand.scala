package ru.d10xa.jadd.show

import cats._
import cats.implicits._
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.d10xa.jadd.ProjectFileReader
import ru.d10xa.jadd.experimental.CodeBlock
import ru.d10xa.jadd.generated.antlr.SbtDependenciesBaseVisitor
import ru.d10xa.jadd.generated.antlr.SbtDependenciesLexer
import ru.d10xa.jadd.generated.antlr.SbtDependenciesParser
import ru.d10xa.jadd.regex.SbtVerbalExpressions

import scala.collection.JavaConverters.asScalaBufferConverter

class SbtShowCommand(
  buildFileSource: String,
  projectFileReader: ProjectFileReader) {
  import SbtShowCommand._

  def show(): String = {

    val blocks: Seq[CodeBlock] = Seq("Seq", "List", "Vector")
      .map(s => s"libraryDependencies ++= $s(")
      .flatMap(CodeBlock.find(buildFileSource, _))

    def lexer(blockInner: String): SbtDependenciesLexer =
      new SbtDependenciesLexer(CharStreams.fromString(blockInner))

    def parser(lexer: SbtDependenciesLexer): SbtDependenciesParser =
      new SbtDependenciesParser(new CommonTokenStream(lexer))

    val multiple: Seq[Art] = blocks.map(_.innerContent).flatMap {
      innerContent =>
        val p = parser(lexer(innerContent))
        val v = new LibraryDependenciesVisitor()
        v.visitMultipleDependencies(p.multipleDependencies())
    }

    val list: List[String] =
      SbtVerbalExpressions.singleLibraryDependency
        .getTextGroups(buildFileSource, 1)
        .asScala
        .toList

    val visitor = new SingleDependencyVisitor()

    val single = list
      .map(lexer)
      .map(parser)
      .map(_.singleDependency())
      .flatMap(visitor.visitSingleDependency)

    val fromDependenciesExternalFile: Seq[Art] =
      if (buildFileSource.contains("import Dependencies._")) {
        def tupleToArt(t: (String, String, String, String)): Art =
          Art(
            g = t._1,
            a = t._3,
            v = t._4,
            isScala = t._2 == "%%",
            scope = None)
        import ru.d10xa.jadd.regex.RegexImplicits._
        projectFileReader
          .read("project/Dependencies.scala")
          .map { source =>
            SbtVerbalExpressions.declaredDependency.groups4(source)
          }
          .map { tuples =>
            tuples.map(tupleToArt)
          }
          .unsafeRunSync()
      } else {
        Seq.empty
      }

    implicit val showArt: Show[Art] =
      Show[Art](
        a =>
          if (a.isScala) s"${a.g}:${a.a}_2.12:${a.v}"
          else s"${a.g}:${a.a}:${a.v}"
      )

    (multiple ++ single ++ fromDependenciesExternalFile).distinct
      .map(_.show)
      .mkString("\n")
  }

}

object SbtShowCommand {
  case class Art(
    g: String,
    a: String,
    v: String,
    scope: Option[String],
    isScala: Boolean)

  class LibraryDependenciesVisitor
      extends SbtDependenciesBaseVisitor[List[Art]] {

    override def visitMultipleDependencies(
      ctx: SbtDependenciesParser.MultipleDependenciesContext
    ): List[Art] = {
      val v = new SingleDependencyVisitor
      ctx.singleDependency().asScala.toList.flatMap(v.visitSingleDependency)
    }
  }

  class SingleDependencyVisitor
      extends SbtDependenciesBaseVisitor[Option[Art]] {
    override def visitSingleDependency(
      ctx: SbtDependenciesParser.SingleDependencyContext
    ): Option[Art] = {
      val arr: List[String] =
        ctx
          .ScalaString()
          .asScala
          .toList
          .map(_.getText)
          .filter(_ != null)
          .filter(_.startsWith("\""))
          .filter(_.endsWith("\""))
          .map(_.drop(1).dropRight(1))
      val isScala =
        Option(ctx.percents())
          .map(_.getText)
          .exists(_.length == 2)
      arr match {
        case g :: a :: v :: scope :: Nil =>
          Some(Art(g, a, v, Some(scope), isScala))
        case g :: a :: v :: Nil =>
          Some(Art(g, a, v, None, isScala))
        case _ => None
      }
    }
  }

  class PercentsVisitor extends SbtDependenciesBaseVisitor[Int] {
    override def visitPercents(
      ctx: SbtDependenciesParser.PercentsContext
    ): Int =
      ctx.getText.length
  }
}
