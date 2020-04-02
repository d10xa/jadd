package ru.d10xa.jadd.github

import cats.ApplicativeError
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import github4s.Github
import github4s.GithubResponses.GHResponse
import github4s.domain.Content
import ru.d10xa.jadd.core.types
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.core.types.MonadThrowable
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.github.GithubUrlParser.GithubUrlParts

class GithubFileOps[F[_]: MonadThrowable](
  github: Github[F],
  owner: String,
  repo: String,
  ref: Option[String])
    extends FileOps[F] {

  def responseToFsItem(r: GHResponse[NonEmptyList[Content]]): F[FsItem] =
    r match {
      case GHResponse(_, 404, _) => FsItem.FileNotFound.pure[F].widen[FsItem]
      case GHResponse(Right(result), _, _) =>
        resultToFsItem(result).pure[F]
      case GHResponse(Left(e), _, _) =>
        ApplicativeError[F, Throwable].raiseError(e)
    }

  def resultToFsItem(r: NonEmptyList[Content]): FsItem =
    r match {
      case NonEmptyList(head, Nil) if head.`type` == "file" =>
        TextFile(
          FileContent(
            head.content
              .map(s => new String(java.util.Base64.getMimeDecoder.decode(s)))
              .getOrElse("")))
      case nel =>
        val files = nel
          .map(_.name)
          .toList
          .map(FileName.apply)
        FsItem.Dir(files)
    }

  override def read(fileName: types.FileName): F[types.FsItem] =
    github.repos
      .getContents(owner, repo, fileName.value, ref)
      .flatMap(responseToFsItem)

  override def write(fileName: types.FileName, value: String): F[Unit] = ???
}

object GithubFileOps {
  def make[F[_]: Sync](gh: Github[F], p: GithubUrlParts): F[FileOps[F]] =
    Sync[F].delay(
      new GithubFileOps[F](gh, owner = p.owner, repo = p.repo, ref = p.ref))
}
