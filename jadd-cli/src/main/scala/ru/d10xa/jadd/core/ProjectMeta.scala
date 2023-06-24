package ru.d10xa.jadd.core

import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.github.GithubUrlParser.GithubUrlParts

final case class ProjectMeta(
  path: Option[String] = None,
  scalaVersion: Option[ScalaVersion] = None,
  githubUrlParts: Option[GithubUrlParts] = None
)
