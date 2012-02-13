//-----------------------------------------------------------------------------
// The MIT License
//
// Copyright (c) 2012 Rick Beton <rick@bigbeeconsultants.co.uk>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//-----------------------------------------------------------------------------

package uk.co.bigbeeconsultants.lhc

import java.net.HttpURLConnection
import collection.mutable.ListBuffer
import org.jcsp.lang.{PoisonException, Channel, Any2OneChannel}

/**
 * The cleanup thread exists so that connections do not need to be closed immediately, which improves
 * HTTP1.1 keep-alive performance. The thread receives unclosed connection wrappers and closes them
 * in batches whenever a size limit is reached.
 * <p>
 * The interface to the cleanup thread is via an unbuffered channel (JCSP) that provides all inter-thread
 * synchronisation and thereby keeps the design simple and efficient.
 */
private[lhc] object CleanupThread extends Thread {
  val limit = 1000

  private val channel: Any2OneChannel[Either[HttpURLConnection, Boolean]] = Channel.any2one(0)
  private val zombies = new ListBuffer[HttpURLConnection]
  private var running = true

  setName("httpCleanup")
  start()

  /**
   * Adds a keep-alive connection to the list of those that will be cleaned up later.
   */
  def futureClose(connWrapper: HttpURLConnection) {
    require (running)
    try {
      channel.out.write(Left(connWrapper))
    }
    catch {
      case pe: PoisonException =>
        throw new IllegalStateException("CleanupThread has already been shut down", pe)
    }
  }

  /**
   * Closes all the keep-alive connections still pending.
   */
  def closeConnections() {
    require (running)
    channel.out.write(Right(true))
  }

  /**
   * Terminates the cleanup thread.
   */
  def terminate() {
    require (running)
    channel.out.write(Right(false))
    while (running)
      Thread.sleep(1)
  }

  /** DO NOT CALL THIS */
  override def run() {
    while (running) {
      channel.in.read match {
        case Left(connWrapper) =>
          zombies += connWrapper
          if (zombies.size > limit) cleanup()
        case Right(flag) =>
          cleanup()
          running = flag
      }
    }
    channel.in.poison(1)
    println(getName + " terminated")
  }

  /** Tests the state of the thread. */
  def isRunning = running

  private def cleanup() {
    for (conn <- zombies) conn.disconnect()
    zombies.clear()
  }
}
