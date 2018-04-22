package ru.d10xa.jadd

import ru.d10xa.jadd.Utils.MetadataUri

object troubles {

  sealed abstract class ArtifactTrouble

  case class ArtifactNotFoundByAlias(alias: String) extends ArtifactTrouble

  case object WrongArtifactRaw extends ArtifactTrouble

  case class LoadVersionsTrouble(metadataUri: MetadataUri, message: String) extends ArtifactTrouble

  def handleTroubles(troubles: Seq[ArtifactTrouble], action: String => Unit): Unit = {
    troubles.map {
      case ArtifactNotFoundByAlias(alias) => s"artifact alias not found ($alias)"
      case WrongArtifactRaw => "artifact syntax invalid"
      case LoadVersionsTrouble(uri, message) =>
        s"some error occurred during versions receive. URI ${uri.uri}. Cause: $message"
    }.foreach(action)
  }

}
