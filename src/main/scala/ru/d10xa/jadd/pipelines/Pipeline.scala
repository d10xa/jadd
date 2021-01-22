package ru.d10xa.jadd.pipelines

import cats.syntax.all._
import cats.data.Chain
import cats.data.Ior
import cats.data.IorNel
import cats.effect.Sync
import ru.d10xa.jadd.cli.Command.Install
import ru.d10xa.jadd.cli.Command.Search
import ru.d10xa.jadd.cli.Command.Show
import ru.d10xa.jadd.cli.Config
import ru.d10xa.jadd.show.JaddFormatShowPrinter
import ru.d10xa.jadd.core.Artifact
import ru.d10xa.jadd.core.Ctx
import ru.d10xa.jadd.core.Loader
import ru.d10xa.jadd.core.ProjectMeta
import ru.d10xa.jadd.core.types.ScalaVersion
import ru.d10xa.jadd.core.Utils
import ru.d10xa.jadd.core.troubles
import ru.d10xa.jadd.core.troubles.ArtifactTrouble
import ru.d10xa.jadd.core.troubles.logTroubles
import ru.d10xa.jadd.log.Logger
import ru.d10xa.jadd.shortcuts.ArtifactShortcuts
import ru.d10xa.jadd.versions.ScalaVersions
import ru.d10xa.jadd.versions.VersionTools

abstract class Pipeline[F[_]: Sync] {

  def invokeCommand(
    ior: IorNel[ArtifactTrouble, List[Artifact]],
    action: List[Artifact] => F[Unit]
  )(implicit logger: Logger[F]): F[Unit] = {
    val handle: List[ArtifactTrouble] => F[Unit] =
      troubles => logTroubles(troubles)

    ior match {
      case Ior.Right(artifacts) =>
        action(artifacts)
      case Ior.Left(troubles) =>
        handle(troubles.toList)
      case Ior.Both(troubles, artifacts) =>
        action(artifacts) >> handle(troubles.toList)
    }
  }

  def findScalaVersion(): F[Option[ScalaVersion]]

  def handleInstall(
    ior: IorNel[ArtifactTrouble, List[Artifact]]
  )(implicit logger: Logger[F]): F[Unit] =
    invokeCommand(ior, artifacts => install(artifacts))

  def handleSearch(
    ior: IorNel[ArtifactTrouble, List[Artifact]]
  )(implicit logger: Logger[F]): F[Unit] =
    invokeCommand(ior, artifacts => search(artifacts))

  def readScalaVersion(): F[ScalaVersion] =
    for {
      predefinedScalaVersion <- Sync[F].pure(ctx.config.scalaVersion)
      optScalaVersion <- findScalaVersion()
    } yield predefinedScalaVersion
      .orElse(optScalaVersion)
      .getOrElse(ScalaVersions.defaultScalaVersion)

  def run(
    loader: Loader[F],
    versionTools: VersionTools[F],
    artifactShortcuts: ArtifactShortcuts
  )(implicit logger: Logger[F]): F[Unit] = {
    def loaded: F[IorNel[troubles.ArtifactTrouble, List[Artifact]]] =
      for {
        scalaVersion <- readScalaVersion()
        newCtx = ctx.copy(meta = ProjectMeta(scalaVersion = Some(scalaVersion)))
        res <- loader.load(
          newCtx,
          versionTools,
          artifactShortcuts
        )
      } yield res
    ctx.config.command match {
      case Show =>
        show()
          .map(_.toList)
          .map(ctx.config.showPrinter.mkString(_))
          .flatMap(s => logger.info(s))
      case Search =>
        loaded.flatMap(handleSearch)
      case Install =>
        loaded.flatMap(handleInstall)
      case command =>
        logger.info(s"command $command not implemented")
    }
  }

  def search(artifacts: List[Artifact])(implicit logger: Logger[F]): F[Unit] =
    for {
      artifactsWithVersions <- artifacts.map(_.inlineScalaVersion).pure[F]
      _ <- logger.info(ctx.config.showPrinter.mkString(artifactsWithVersions))
      stringsForPrint = artifactsWithVersions
        .map(artifact =>
          JaddFormatShowPrinter.withVersions
            .single(artifact) + " // " + artifact.versionsForPrint
        )
      _ <- logger.debug(stringsForPrint.mkString("\n"))
    } yield ()

  def install(artifacts: List[Artifact])(implicit logger: Logger[F]): F[Unit]
  def show()(implicit logger: Logger[F]): F[Chain[Artifact]]
  def ctx: Ctx

}

object Pipeline {

  def requirementToArtifacts[F[_]: Sync](
    requirementResourcePath: String
  ): F[Seq[String]] =
    Utils
      .mkStringFromResourceF(requirementResourcePath)
      .map(_.trim.split("\\r?\\n").map(_.trim).toVector)

  def fromRequirements[F[_]: Sync](config: Config): F[Option[Seq[String]]] =
    config.requirements match {
      case requirements if requirements.nonEmpty =>
        val artifactStrings = requirements.toList
          .flatTraverse(requirement =>
            requirementToArtifacts(requirement).map(_.toList)
          )
        artifactStrings.map(_.some)
      case _ => Sync[F].pure(None)
    }

  val fromConfig: Config => Option[Seq[String]] = _.artifacts.some

  def extractArtifacts[F[_]: Sync](ctx: Ctx): F[Seq[String]] =
    for {
      fromReq <- fromRequirements(ctx.config)
    } yield fromReq.orElse(fromConfig(ctx.config)).getOrElse(Seq.empty)

}
