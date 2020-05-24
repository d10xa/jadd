package ru.d10xa.jadd.code.scalameta

import scala.meta.Lit
import scala.meta.Term

/**
  *
  * sbt.librarymanagement.DependencyBuilders.OrganizationArtifactName
  *
  * "org.something" %% "something-name"
  *
  * @param groupId org.something
  * @param artifactId something-name
  * @param percents %%
  */
final case class GroupIdPercentArtifactId(
  groupId: String,
  artifactId: String,
  percents: PercentChars)

object GroupIdPercentArtifactIdMatch {
  def unapply(t: Term.ApplyInfix): Option[GroupIdPercentArtifactId] =
    t match {
      case Term
            .ApplyInfix(
            Lit.String(groupId),
            Term.Name(PercentCharsMatch(p)),
            Nil,
            List(Lit.String(artifactId))) =>
        Some(GroupIdPercentArtifactId(groupId, artifactId, PercentChars(p)))
      case _ => None
    }
}
