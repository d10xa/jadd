package ru.d10xa.jadd.fs

import cats.ApplicativeThrow
import monocle.Prism
import monocle.macros.GenPrism

import java.nio.file.Path
import ru.d10xa.jadd.core.types.FileContent

sealed trait FsItem

object FsItem {
  final case class TextFile(content: FileContent, path: Path) extends FsItem {
    def map(f: String => String): TextFile =
      this.copy(content = FileContent(f(content.value)))
  }

  object TextFile {
    def make[F[_]](
      fsItem: FsItem
    )(implicit a: ApplicativeThrow[F]): F[TextFile] =
      fsItem match {
        case t: TextFile => a.pure(t)
        case _: Dir =>
          a.raiseError[TextFile](
            new IllegalArgumentException(s"Is not a file")
          )
        case FileNotFound =>
          a.raiseError[TextFile](
            new IllegalArgumentException("File not found")
          )
      }
  }

  final case class Dir(path: Path, files: List[Path]) extends FsItem
  final case object FileNotFound extends FsItem

  val textFilePrism: Prism[FsItem, TextFile] =
    GenPrism[FsItem, TextFile]

}
