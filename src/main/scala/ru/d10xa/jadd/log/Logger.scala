package ru.d10xa.jadd.log

import cats.effect.Sync
import ru.d10xa.jadd.log.Logger.Render

trait Logger[F[_]] {
  def info[M](msg: => M)(implicit render: Render[M]): F[Unit]
  def debug[M](msg: => M)(implicit render: Render[M]): F[Unit]
}

object Logger {

  def apply[F[_]](implicit logger: Logger[F]): Logger[F] = logger

  def make[F[_]: Sync](debug: Boolean, quiet: Boolean): Logger[F] =
    if (quiet) new DisabledLogger[F]
    else if (debug) new DebugLogger[F]
    else new InfoLogger[F]

  trait Render[A] {
    def render(a: A): String
  }

  object Render {
    implicit val renderString: Render[String] = s => identity[String](s)
  }

  private class DisabledLogger[F[_]: Sync] extends Logger[F] {
    override def info[M](msg: => M)(implicit render: Render[M]): F[Unit] =
      Sync[F].unit
    override def debug[M](msg: => M)(implicit render: Render[M]): F[Unit] =
      Sync[F].unit
  }

  private class InfoLogger[F[_]: Sync] extends Logger[F] {
    override def info[M](msg: => M)(implicit render: Render[M]): F[Unit] =
      Sync[F].delay(println(msg))
    override def debug[M](msg: => M)(implicit render: Render[M]): F[Unit] =
      Sync[F].unit
  }

  private class DebugLogger[F[_]: Sync] extends InfoLogger[F] {
    override def debug[M](msg: => M)(implicit render: Render[M]): F[Unit] =
      Sync[F].delay(println(msg))
  }
}
