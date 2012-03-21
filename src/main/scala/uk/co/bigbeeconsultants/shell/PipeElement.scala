package uk.co.bigbeeconsultants.shell

import java.io.PrintStream
import scala.io.{Source => IoSource}

abstract class PipeElement(shell: Shell = new Shell) {

  protected def exec: Iterator[String]

  def -|(next: String) = {
    new ShellCommand (next, exec, shell)
  }

  def -->(predicate: (String) => Boolean) = {
    new LineFilter (predicate, exec, shell)
  }

  //  def |(fn: (String) => String) = {
  //    new LineChanger (fn, exec, shell)
  //  }

  def |>(sink: Sink) = {
    sink.put (exec)
    this
  }
}


class LineChanger(change: (String) => String,
                  pipeIn: Iterator[String],
                  shell: Shell) extends PipeElement (shell) {

  protected def exec: Iterator[String] = new ChangeIterator (change, pipeIn)
}

private class ChangeIterator(change: (String) => String,
                             upstream: Iterator[String]) extends Iterator[String] {
  def hasNext = upstream.hasNext

  def next() = change (upstream.next ())
}


class LineFilter(predicate: (String) => Boolean,
                 pipeIn: Iterator[String],
                 shell: Shell) extends PipeElement (shell) {

  protected def exec: Iterator[String] = new FilterIterator (predicate, pipeIn)
}

private class FilterIterator(predicate: (String) => Boolean,
                             upstream: Iterator[String]) extends Iterator[String] {
  private var nextLine: String = null

  private def prefetch() {
    while (upstream.hasNext && nextLine == null) {
      nextLine = upstream.next ()
      if (!predicate (nextLine)) {
        nextLine == null
      }
    }
  }

  def hasNext = nextLine != null

  def next() = {
    if (nextLine == null) {
      upstream.next ()
    } // force exception
    val result = nextLine
    prefetch ()
    result
  }

}


class ShellCommand(command: String,
                   pipeIn: Iterator[String] = Nil.iterator,
                   shell: Shell = new Shell) extends PipeElement (shell) {

  protected def exec: Iterator[String] = {
    if (shell.echo) println (command)
    val process = Runtime.getRuntime.exec (command)

    if (!pipeIn.isEmpty) {
      val os = new PrintStream (process.getOutputStream, true)
      pipeIn.foreach (os.println (_))
      os.close ()
    }

    IoSource.fromInputStream (process.getErrorStream).getLines ().foreach (System.err.println (_))
    new OutputIterator (IoSource.fromInputStream (process.getInputStream).getLines (), process)
  }

  private class OutputIterator(output: Iterator[String], process: Process) extends Iterator[String] {
    var outputting = true

    def hasNext = {
      val n = output.hasNext
      if (n != outputting) {
        shell.handleExitCode (process.waitFor)
        outputting = false
      }
      n
    }

    def next() = output.next ()
  }

}
