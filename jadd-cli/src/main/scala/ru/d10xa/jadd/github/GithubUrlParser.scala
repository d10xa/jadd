package ru.d10xa.jadd.github

import cats.ApplicativeThrow
import cats.MonadThrow
import cats.syntax.all._
import io.lemonlabs.uri.Url
import ru.d10xa.jadd.github.GithubUrlParser.GithubUrlParts

trait GithubUrlParser[F[_]] {
  def parse(url: String): F[GithubUrlParts]
}

object GithubUrlParser {
  final case class GithubUrlParts(
    owner: String,
    repo: String,
    file: Option[String],
    ref: Option[String]
  )
}

class LiveGithubUrlParser[F[_]: MonadThrow] private ()
    extends GithubUrlParser[F] {

  override def parse(url: String): F[GithubUrlParts] =
    toUrl(url).flatMap(parseUrl)

  def toUrl(url: String): F[Url] =
    ApplicativeThrow[F].fromTry(Url.parseTry(url))

  def parseUrl(url: Url): F[GithubUrlParts] =
    url.path match {
      case p @ io.lemonlabs.uri.PathParts(owner, repo, "blob", _*) =>
        GithubUrlParts(
          owner = owner,
          repo = repo,
          file = {
            val x = p.parts.drop(4)
            if (x.nonEmpty) { Some(x.mkString("/")) }
            else { None }
          },
          ref = p.parts.lift(3)
        ).pure[F]
      case io.lemonlabs.uri.PathParts(owner, repo) =>
        GithubUrlParts(
          owner = owner,
          repo = repo,
          file = None,
          ref = None
        ).pure[F]
      case _ =>
        ApplicativeThrow[F]
          .raiseError(new RuntimeException("can not parse github url"))
    }

}

object LiveGithubUrlParser {
  def make[F[_]: MonadThrow](): GithubUrlParser[F] =
    new LiveGithubUrlParser[F]()
}
