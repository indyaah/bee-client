package uk.co.bigbeeconsultants.shell

import scala._
import scala.collection.Iterator
import scala.io.{Source => IoSource}
import java.io._

class Shell(val echo: Boolean = false, val handleExitCode: (Int) => Unit = (x) => {}) {

  implicit def cmd(command: String) = new ShellCommand (command, Nil.iterator, this)

  def echoCommands = {
    new Shell (true, handleExitCode)
  }

  def exitOnError = {
    new Shell (echo, (exitCode) => if (exitCode != 0) System.exit (exitCode))
  }

  def exec(command: String) = {
    new ShellCommand (command, Nil.iterator, this)
  }

  def cat(file: EncFile): PipeElement = {
    cat (new TextFile (file))
  }

  def cat(source: Source): PipeElement = {
    new LineFilter ((x) => true, source.get (1, Int.MaxValue), this)
  }

  def cat(iterable: Iterable[String]): PipeElement = {
    new LineFilter ((x) => true, iterable.iterator, this)
  }
}

object Shell {
  def apply() = new Shell ()

  def echoCommands = {
    new Shell (true)
  }

  def exitOnError = {
    new Shell (handleExitCode = (exitCode) => if (exitCode != 0) System.exit (exitCode))
  }

  implicit def toSource(iterable: Iterable[String]): Source = {
    new Source {
      def get(from: Int = 1, maxLines: Int = Int.MaxValue) = iterable.iterator
    }
  }

  def env(name: String) = System.getenv (name)
}


trait Source {
  def get(from: Int = 1, maxLines: Int = Int.MaxValue): Iterator[String]
}


trait Sink {
  def put(lines: TraversableOnce[String])
}


object Stdout extends Sink {
  def put(lines: TraversableOnce[String]) {
    lines.foreach (println)
  }
}


class TextFile(file: EncFile) extends Source with Sink {

  def get: Iterator[String] = {
    get (1, Int.MaxValue)
  }

  def get(from: Int = 1, maxLines: Int = Int.MaxValue): Iterator[String] = {
    IoSource.fromFile (file.name, file.encoding).getLines ()
  }

  def put(lines: TraversableOnce[String]) {
    val pw = printWriter (false)
    lines.foreach (pw.println (_))
    pw.close ()
  }

  def append(lines: TraversableOnce[String]) {
    val pw = printWriter (true)
    lines.foreach (pw.println (_))
    pw.close ()
  }

  private def printWriter(append: Boolean) = new PrintWriter (new OutputStreamWriter (new FileOutputStream (file.name, append), file.encoding))
}


object TextFile {
  def apply(fileName: String, encoding: String = "UTF-8") = new TextFile (EncFile (fileName, encoding))
}
