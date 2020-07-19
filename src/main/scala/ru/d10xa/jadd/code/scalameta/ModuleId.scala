package ru.d10xa.jadd.code.scalameta

import monocle.Lens
import monocle.macros.GenLens

import scala.meta.Term

/**
  *
  * ModuleID in terms of SBT
  *
  * "org.something" %% "something-name" % "0.0.1"
  *
  */
final case class ModuleId(
  groupId: String,
  percentsCount: Int,
  artifactId: String,
  version: ModuleVersion,
  terms: List[Term]
)

object ModuleId {
  val termsLens: Lens[ModuleId, List[Term]] = GenLens[ModuleId](_.terms)
}

object ModuleIdMatch {
  def unapply(t: Term.ApplyInfix): Option[ModuleId] =
    t match {
      case ApplyInfixPercent(
          GroupIdPercentArtifactIdMatch(ga),
          1,
          ModuleVersionMatch(moduleVersion)) =>
        Some(
          ModuleId(
            groupId = ga.groupId,
            percentsCount = ga.percents.n,
            artifactId = ga.artifactId,
            version = moduleVersion,
            terms = List.empty[Term]
          ))
      case ApplyInfixPercent(ModuleIdMatch(mId), 1, term) =>
        Some(ModuleId.termsLens.modify(l => term :: l)(mId))
      case _ => None
    }
}
