package ru.d10xa.jadd.code.scalameta

import cats.Applicative
import coursier.core.Version
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Scope
import ru.d10xa.jadd.core.types.GroupId
import ru.d10xa.jadd.core.types.ScalaVersion

import scala.meta.Source
import scala.meta.Term
import cats.syntax.all._

trait SbtArtifactsParser[F[_]] {
  def parse(
    scalaVersion: ScalaVersion,
    sources: Vector[Source]
  ): F[Vector[Artifact]]
}

object SbtArtifactsParser {
  def make[F[_]: Applicative](
    sbtModuleIdFinder: SbtModuleIdFinder,
    sbtStringValFinder: SbtStringValFinder
  ): F[SbtArtifactsParser[F]] = {
    def parsePure(
      scalaVersion: ScalaVersion,
      sources: Vector[Source]
    ): Vector[Artifact] = {
      val moduleIds: Vector[ModuleId] =
        sources.flatMap(sbtModuleIdFinder.find).distinct
      val stringValsMap: Map[String, String] = sources
        .flatMap(sbtStringValFinder.find)
        .map { case StringVal(name, value) => name -> value }
        .toMap
      moduleIds.map(m =>
        Artifact(
          groupId = GroupId(m.groupId),
          artifactId = if (m.percentsCount > 1) {
            s"${m.artifactId}%%"
          } else m.artifactId,
          maybeVersion = m.version match {
            case VersionString(v) => Version(v).some
            case VersionVal(v) => stringValsMap.get(v).map(Version(_))
          },
          maybeScalaVersion = if (m.percentsCount > 1) {
            scalaVersion.some
          } else None,
          scope = m.terms
            .find {
              case Term.Name("Test") => true
              case _ => false
            }
            .map(_ => Scope.Test)
        )
      )
    }
    new SbtArtifactsParser[F] {
      override def parse(
        scalaVersion: ScalaVersion,
        sources: Vector[Source]
      ): F[Vector[Artifact]] = parsePure(scalaVersion, sources).pure[F]
    }.pure[F]
  }
}
