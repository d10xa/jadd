package ru.d10xa.jadd.code.scalameta

import cats.Applicative
import cats.syntax.all._
import coursier.core.Version
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Module
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion

import java.nio.file.Path
import scala.meta.Source
import scala.meta.Term

trait SbtArtifactsParser[F[_]] {
  def parseArtifacts(
    scalaVersion: ScalaVersion,
    sources: Vector[(Path, Source)]
  ): F[Vector[Artifact]]
}

object SbtArtifactsParser {

  def make[F[_]: Applicative](
    sbtModuleParser: SbtModuleParser[F]
  ): F[SbtArtifactsParser[F]] =
    Applicative[F].pure {
      (scalaVersion: ScalaVersion, sources: Vector[(Path, Source)]) =>
        sbtModuleParser
          .parse(sources)
          .map(_.map(r => moduleToArtifact(scalaVersion, r)))
    }

  def moduleToArtifact(scalaVersion: ScalaVersion, m: Module): Artifact =
    m match {
      case Module(
            groupIdVariableValue,
            percentsCount,
            artifactIdVariableValue,
            version,
            terms
          ) =>
        val groupId = groupIdVariableValue match {
          case VariableLit(value, _) => value
          case VariableLitP(VariableLit(value, _), _) => value
          case VariableTerms(values) => values.mkString(".")
        }
        val artifactId = artifactIdVariableValue match {
          case VariableLit(value, _) => value
          case VariableLitP(VariableLit(value, _), _) => value
          case VariableTerms(values) => values.mkString(".")
        }

        Artifact(
          groupId = GroupId(groupId),
          artifactId = if (percentsCount > 1) s"$artifactId%%" else artifactId,
          maybeVersion = version match {
            case VariableLit(value, _) => Version(value).some
            case VariableLitP(VariableLit(value, _), _) => Version(value).some
            case VariableTerms(_) => None // TODO think what to do
          },
          maybeScalaVersion = if (percentsCount > 1) {
            scalaVersion.some
          } else None,
          scope = terms
            .find {
              case Term.Name("Test") => true
              case _ => false
            }
            .map(_ => ru.d10xa.jadd.core.Scope.Test)
        )
      case _ =>
        throw new IllegalStateException(
          s"This branch is not reachable. LitString is only possible" +
            s" case for groupId and artifactId(at least now)"
        )
    }

}
