package ru.d10xa.jadd.code.inserts

import cats.Monad
import cats.syntax.all._
import coursier.core.Version
import ru.d10xa.jadd.code.inserts.SbtFileUpserts.SbtUpsertQuery
import ru.d10xa.jadd.code.scalameta.SbtParser
import ru.d10xa.jadd.code.scalameta.ScalaMetaPatternMatching.Module
import ru.d10xa.jadd.code.scalameta.VariableLit
import ru.d10xa.jadd.code.scalameta.VariableLitP
import ru.d10xa.jadd.code.scalameta.VariableValue
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.log.Logger

trait SbtFileUpserts[F[_]] {
  def upsert(artifacts: Seq[Artifact]): F[SbtUpsertQuery]
}

object SbtFileUpserts {

  final case class SbtUpsertQuery(
    toInsert: Vector[Artifact],
    toUpdate: Vector[(VariableLitP, Version)]
  )

  def make[F[_]: Monad: Logger](
    sbtParser: SbtParser[F],
    fileOps: FileOps[F]
  ): F[SbtFileUpserts[F]] = new SbtFileUpserts[F] {
    override def upsert(
      artifacts: Seq[Artifact]
    ): F[SbtUpsertQuery] = for {
      sbtParseResult <- sbtParser.parse(fileOps)
      artifactsToInsert = artifacts.filter(a =>
        !sbtParseResult.modules.exists(m =>
          equalsNoVersionVV(a, m.groupId, m.artifactId)
        )
      )
      ms = sbtParseResult.modules
        .flatMap {
          case m @ Module(
                VariableLitP(VariableLit(groupId, _), groupIdFilePath),
                percentsCount,
                VariableLitP(VariableLit(artifactId, _), artifactIdFilePath),
                VariableLitP(VariableLit(version, _), versionFilePath),
                terms
              ) =>
            artifacts
              .find(equalsNoVersion(_, groupId, artifactId))
              .map(art => art -> m) // TODO match %%
          case _ => List.empty
        }
        .collect {
          case tuple if tuple._2.version.isInstanceOf[VariableLitP] =>
            (tuple._2.version.asInstanceOf[VariableLitP], tuple._1, tuple._2)
        }
      toUpdate = ms.flatMap {
        case (vlp: VariableLitP, a: Artifact, mod: Module) =>
          a.maybeVersion match {
            case Some(version) => Vector(vlp -> version)
            case None => Vector.empty
          }
      }
      upsertQuery = SbtUpsertQuery(
        toInsert = artifactsToInsert.toVector,
        toUpdate = toUpdate
      )
    } yield upsertQuery
  }.pure[F]

  def equalsNoVersion(
    a: Artifact,
    groupId: String,
    artifactId: String
  ): Boolean =
    a.artifactId == artifactId && groupId === a.groupId.value

  def equalsNoVersionVV(
    a: Artifact,
    groupId: VariableValue,
    artifactId: VariableValue
  ): Boolean = (groupId.valueOption, artifactId.valueOption) match {
    case (Some(groupId), Some(artifactId)) =>
      groupId === a.groupId.value &&
        artifactId === a.artifactId
    case _ => false
  }
}
