package ru.d10xa.jadd.pipelines

import cats.data.Ior
import cats.data.IorNel
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
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
import ru.d10xa.jadd.core.troubles.handleTroubles
import ru.d10xa.jadd.versions.ScalaVersions

trait Pipeline extends StrictLogging {
  def applicable[F[_]: Sync](): F[Boolean]

  def invokeCommand[F[_]: Sync](
    ior: IorNel[ArtifactTrouble, List[Artifact]],
    action: List[Artifact] => F[Unit]
  ): F[Unit] = {
    val handle: List[ArtifactTrouble] => F[Unit] =
      troubles => Sync[F].delay(handleTroubles(troubles, s => logger.info(s)))

    ior match {
      case Ior.Right(artifacts) =>
        action(artifacts)
      case Ior.Left(troubles) =>
        handle(troubles.toList)
      case Ior.Both(troubles, artifacts) =>
        action(artifacts) >> handle(troubles.toList)
    }
  }

  def findScalaVersion[F[_]: Sync](): F[Option[ScalaVersion]]

  def handleInstall[F[_]: Sync](
    ior: IorNel[ArtifactTrouble, List[Artifact]]
  ): F[Unit] =
    invokeCommand(ior, artifacts => install(artifacts))

  def handleSearch[F[_]: Sync](
    ior: IorNel[ArtifactTrouble, List[Artifact]]
  ): F[Unit] =
    invokeCommand(ior, artifacts => Sync[F].delay(search(artifacts)))

  def readScalaVersion[F[_]: Sync](): F[ScalaVersion] =
    for {
      predefinedScalaVersion <- Sync[F].pure(ctx.config.scalaVersion)
      optScalaVersion <- findScalaVersion()
    } yield
      predefinedScalaVersion
        .orElse(optScalaVersion)
        .getOrElse(ScalaVersions.defaultScalaVersion)

  def run[F[_]: Sync](
    loader: Loader
  ): F[Unit] = {
    def loaded: F[IorNel[troubles.ArtifactTrouble, List[Artifact]]] =
      for {
        scalaVersion <- readScalaVersion()
        res <- loader.load(
          ctx.copy(meta = ProjectMeta(scalaVersion = Some(scalaVersion))))
      } yield res
    ctx.config.command match {
      case Show =>
        show()
          .map(_.toList)
          .map(ctx.config.showPrinter.mkString(_))
          .map(s => logger.info(s))
      case Search =>
        loaded.flatMap(handleSearch[F])
      case Install =>
        loaded.flatMap(handleInstall[F])
      case command =>
        Sync[F].delay(logger.info(s"command $command not implemented"))
    }
  }

  def search(artifacts: List[Artifact]): Unit = {
    val artifactsWithVersions = artifacts.map(_.inlineScalaVersion)
    logger.info(ctx.config.showPrinter.mkString(artifactsWithVersions))
    val stringsForPrint = artifactsWithVersions
      .map(
        artifact =>
          JaddFormatShowPrinter.withVersions
            .single(artifact) + " // " + artifact.versionsForPrint)
    logger.debug(stringsForPrint.mkString("\n"))
  }

  def install[F[_]: Sync](artifacts: List[Artifact]): F[Unit]
  def show[F[_]: Sync](): F[Seq[Artifact]]
  def ctx: Ctx

}

object Pipeline {

  def requirementToArtifacts[F[_]: Sync](
    requirementResourcePath: String): F[Seq[String]] =
    Utils
      .mkStringFromResourceF(requirementResourcePath)
      .map(_.trim.split("\\r?\\n").map(_.trim).toVector)

  def fromRequirements[F[_]: Sync](config: Config): F[Option[Seq[String]]] =
    config.requirements match {
      case requirements if requirements.nonEmpty =>
        val artifactStrings = requirements.toList
          .flatTraverse(requirement =>
            requirementToArtifacts(requirement).map(_.toList))
        artifactStrings.map(_.some)
      case _ => Sync[F].pure(None)
    }

  val fromConfig: Config => Option[Seq[String]] = _.artifacts.some

  def extractArtifacts[F[_]: Sync](ctx: Ctx): F[Seq[String]] =
    for {
      fromReq <- fromRequirements(ctx.config)
      fromConf = fromConfig(ctx.config)
    } yield fromReq.orElse(fromConf).getOrElse(Seq.empty)

}
