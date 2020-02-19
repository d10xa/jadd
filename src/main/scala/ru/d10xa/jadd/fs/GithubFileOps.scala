package ru.d10xa.jadd.fs

import cats.ApplicativeError
import cats.data.NonEmptyList
import cats.implicits._
import eu.timepit.refined
import eu.timepit.refined.collection.NonEmpty
import github4s.Github
import github4s.GithubResponses.GHResponse
import github4s.GithubResponses.GHResult
import github4s.GithubResponses.JsonParsingException
import github4s.domain.Content
import ru.d10xa.jadd.core.types
import ru.d10xa.jadd.core.types.FileName
import ru.d10xa.jadd.core.types.FsItem
import ru.d10xa.jadd.core.types.FsItem.TextFile
import ru.d10xa.jadd.core.types.FileContent
import ru.d10xa.jadd.core.types.MonadThrowable

class GithubFileOps[F[_]: MonadThrowable](
  github: Github[F],
  owner: String,
  repo: String)
    extends FileOps[F] {

  def responseToFsItem(r: GHResponse[NonEmptyList[Content]]): F[FsItem] =
    r match {
      case Right(value) =>
        resultToFsItem(value).pure[F]
      case Left(JsonParsingException(msg, _))
          if msg.contains("\"message\" : \"Not Found\"") =>
        FsItem.FileNotFound.pure[F].widen[FsItem]
      case Left(gHException) =>
        ApplicativeError[F, Throwable].raiseError(gHException)
    }

  def resultToFsItem(r: GHResult[NonEmptyList[Content]]): FsItem =
    r.result match {
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
          .flatMap(refined.refineV[NonEmpty](_).toOption.toList)
          .map(FileName.apply)
        FsItem.Dir(files)
    }

  override def read(fileName: types.FileName): F[types.FsItem] =
    github.repos
      .getContents(owner, repo, fileName.value.value)
      .flatMap(responseToFsItem)

  override def write(fileName: types.FileName, value: String): F[Unit] = ???
}
