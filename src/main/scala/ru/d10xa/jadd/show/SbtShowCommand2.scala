package ru.d10xa.jadd.show

import cats.data.Chain
import cats.effect.Sync
import cats.implicits._
import coursier.core.Version
import ru.d10xa.jadd.code.scalameta.ModuleIdMatch
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.ScalaVersionFinder
import ru.d10xa.jadd.core.types.FsItem.Dir
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.core.types._
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.versions.ScalaVersions

import scala.meta.Source
import scala.meta.dialects
import scala.meta.parsers.Parsed

class SbtShowCommand2[F[_]: Sync](
  fileOps: FileOps[F],
  scalaVersionFinder: ScalaVersionFinder[F]) {

  def show(): F[Chain[Artifact]] =
    FileName("build.sbt")
      .pure[F]
      .flatMap(fileOps.read)
      .flatMap(TextFile.make[F])
      .map(_.content)
      .flatMap(showFromSource)

  def parseSource(s: String): Parsed[Source] =
    dialects
      .Sbt1(s)
      .parse[Source]

  def showFromSource(fileContent: FileContent): F[Chain[Artifact]] = {
    val buildFileSource = fileContent.value
    val scalaVersionF: F[ScalaVersion] =
      scalaVersionFinder
        .findScalaVersion()
        .map(_.getOrElse(ScalaVersions.defaultScalaVersion))

    val otherSbtFilesF = fileOps.read(FileName("project")).map {
      case Dir(names) => names
      case _ => List.empty[FileName]
    }

    for {
      scalaVersion <- scalaVersionF
      otherSbtFiles <- otherSbtFilesF
      otherScalaSources <- otherSbtFiles
        .filter(n => n.value.endsWith(".sbt") || n.value.endsWith(".scala"))
        .traverse(fileOps.read)
        .map(_.collect { case t: TextFile => t })
      a = (buildFileSource :: otherScalaSources.map(_.content.value)).flatMap {
        str =>
          dialects.Sbt1(str).parse[Source].get.collect {
            case ModuleIdMatch(m) => m
          }
      }
      c = Chain.fromSeq(
        a.map(m =>
          Artifact(
            groupId = GroupId(m.groupId),
            artifactId = m.artifactId,
            maybeVersion = Version(m.version).some,
            maybeScalaVersion = if (m.percentsCount > 1) {
              scalaVersion.some
            } else None
        )))
    } yield c

  }
}
