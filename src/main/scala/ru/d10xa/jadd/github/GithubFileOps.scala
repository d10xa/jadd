package ru.d10xa.jadd.github

import java.nio.file.Path

import cats.ApplicativeError
import cats.data.NonEmptyList
import cats.effect.Bracket
import cats.effect.Resource
import cats.effect.Sync
import github4s.GHResponse
import github4s.Github
import github4s.domain.Content
import ru.d10xa.jadd.fs.FsItem.TextFile
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.MonadThrowable
import ru.d10xa.jadd.fs.FileOps
import ru.d10xa.jadd.fs.FsItem
import ru.d10xa.jadd.github.GithubUrlParser.GithubUrlParts
import ru.d10xa.jadd.instances._

class GithubFileOps[F[_]: MonadThrowable: Bracket[*[_], Throwable]](
  githubResource: Resource[F, Github[F]],
  owner: String,
  repo: String,
  ref: Option[String])
    extends FileOps[F] {

  def responseToFsItem(
    path: Path,
    r: GHResponse[NonEmptyList[Content]]): F[FsItem] =
    r match {
      case GHResponse(_, 404, _) => FsItem.FileNotFound.pure[F].widen[FsItem]
      case GHResponse(Right(result), _, _) =>
        resultToFsItem(path, result).pure[F]
      case GHResponse(Left(e), _, _) =>
        ApplicativeError[F, Throwable].raiseError(e)
    }

  def resultToFsItem(path: Path, r: NonEmptyList[Content]): FsItem =
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
          .map(path.resolve)
        FsItem.Dir(path, files)
    }

  override def read(path: Path): F[FsItem] =
    for {
      response <- githubResource.use(
        _.repos.getContents(owner, repo, path.show, ref))
      fsItem <- responseToFsItem(path, response)
    } yield fsItem

  override def write(path: Path, value: String): F[Unit] = ???
}

object GithubFileOps {
  def make[F[_]: Sync](
    githubResource: Resource[F, Github[F]],
    p: GithubUrlParts): F[FileOps[F]] =
    Sync[F].delay(
      new GithubFileOps[F](
        githubResource,
        owner = p.owner,
        repo = p.repo,
        ref = p.ref))
}
