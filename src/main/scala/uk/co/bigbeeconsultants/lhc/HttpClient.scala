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

import header.{Headers, Header, MediaType}
import response._
import java.net.{URL, HttpURLConnection}
import java.nio.ByteBuffer
import java.io._
import java.util.zip.GZIPInputStream
import collection.mutable.ListBuffer
import request.{Config, RequestException, Body, Request}
import org.jcsp.lang.{PoisonException, Any2OneChannel, Channel}

/**
 * Constructs an instance for handling any number of HTTP requests.
 * <p>
 * By default, HTTP cookies are ignored. If you require support for cookies, use the standard
 * java.net.CookieHandler classes, e.g.
 * <code>java.net.CookieHandler.setDefault( new java.net.CookieManager() )</code>.
 *
 * @param keepAlive true for connections to be used once then closed; false for keep-alive connections
 */
final class HttpClient(keepAlive: Boolean = true,
                       requestConfig: Config = Config.default,
                       commonRequestHeaders: Headers = HttpClient.defaultHeaders,
                       responseBodyFactory: BodyFactory = HttpClient.defaultResponseBodyFactory) {

  private val emptyBuffer = ByteBuffer.allocateDirect(0)

  /**Make a HEAD request. */
  def head(url: URL, requestHeaders: Headers = Headers.none) =
    execute(Request.head(url), requestHeaders)

  /**Make a TRACE request. */
  def trace(url: URL, requestHeaders: Headers = Headers.none) =
    execute(Request.trace(url), requestHeaders)

  /**Make a GET request. */
  def get(url: URL, requestHeaders: Headers = Headers.none) =
    execute(Request.get(url), requestHeaders)

  /**Make a DELETE request. */
  def delete(url: URL, requestHeaders: Headers = Headers.none) =
    execute(Request.delete(url), requestHeaders)

  /**Make an OPTIONS request. */
  def options(url: URL, body: Option[Body], requestHeaders: Headers = Headers.none) =
    execute(Request.options(url, body), requestHeaders)

  /**Make a POST request. */
  def post(url: URL, body: Body, requestHeaders: Headers = Headers.none) =
    execute(Request.post(url, body), requestHeaders)

  /**Make a PUT request. */
  def put(url: URL, body: Body, requestHeaders: Headers = Headers.none) =
    execute(Request.put(url, body), requestHeaders)

  /**Make an arbitrary request. */
  def execute(request: Request, requestHeaders: Headers = Headers.none): Response = {
    val connWrapper = request.url.openConnection.asInstanceOf[HttpURLConnection]
    connWrapper.setAllowUserInteraction(false)
    connWrapper.setConnectTimeout(requestConfig.connectTimeout)
    connWrapper.setReadTimeout(requestConfig.readTimeout);
    connWrapper.setInstanceFollowRedirects(requestConfig.followRedirects)
    connWrapper.setUseCaches(requestConfig.useCaches)

    try {
      setRequestHeaders(request, requestHeaders, connWrapper)

      //      connWrapper.setDoInput(request.method != Request.HEAD)
      connWrapper.setDoOutput(request.method == Request.POST || request.method == Request.PUT)

      connWrapper.connect()

      copyRequestBodyToOutputStream(request, connWrapper)

      val status = Status(connWrapper.getResponseCode, connWrapper.getResponseMessage)
      val result = getContent(status, request, connWrapper)
      if (connWrapper.getResponseCode >= 400) {
        throw new RequestException(request, status, Some(result), None)
      }
      result

    } catch {
      case ioe: IOException =>
        val status = Status(connWrapper.getResponseCode, connWrapper.getResponseMessage)
        throw new RequestException(request, status, None, Some(ioe))

    } finally {
      markConnectionForClosure(connWrapper)
    }
  }

  private def copyRequestBodyToOutputStream(request: Request, connWrapper: HttpURLConnection) {
    if (request.method == Request.POST || request.method == Request.PUT) {
      require(request.body.isDefined, "An entity body is required when making a POST request.")
      if (request.method == Request.POST) {
        request.body.get.copyTo(connWrapper.getOutputStream)
      }
    }
  }

  private def markConnectionForClosure(connWrapper: HttpURLConnection) {
    if (!keepAlive) connWrapper.disconnect()
    else CleanupThread.futureClose(connWrapper)
  }

  def closeConnections() {
    if (keepAlive) CleanupThread.closeConnections()
  }

  private def setRequestHeaders(request: Request, requestHeaders: Headers, connWrapper: HttpURLConnection) {
    val method = if (request.method == null) Request.GET else request.method.toUpperCase
    connWrapper.setRequestMethod(method)

    if (requestConfig.sendHostHeader) {
      connWrapper.setRequestProperty(Header.HOST, request.url.getHost)
    }

    if (request.body.isDefined)
      connWrapper.setRequestProperty(Header.CONTENT_TYPE, request.body.get.mediaType.toString)

    for (hdr <- commonRequestHeaders.list) {
      connWrapper.setRequestProperty(hdr.name, hdr.value)
    }

    for (hdr <- requestHeaders.list) {
      connWrapper.setRequestProperty(hdr.name, hdr.value)
    }
  }

  private def getContent(status: Status, request: Request, connWrapper: HttpURLConnection): Response = {
    val responseHeaders = processResponseHeaders(connWrapper)
    if (request.method == Request.HEAD) {
      val mediaType = MediaType(connWrapper.getContentType)
      Response(status, new BodyCache(mediaType, emptyBuffer), responseHeaders)

    } else {
      val contEnc = responseHeaders.find(Header.CONTENT_ENCODING)
      val mediaType = MediaType(connWrapper.getContentType)
      val body = responseBodyFactory.newBody(mediaType)
      body.receiveData(mediaType, getBodyStream(contEnc, connWrapper))
      Response(status, body, responseHeaders)
    }
  }

  private def getBodyStream(contEnc: List[Header], connWrapper: HttpURLConnection) = {
    val iStream = if (connWrapper.getResponseCode >= 400) connWrapper.getErrorStream else connWrapper.getInputStream
    if (!contEnc.isEmpty && contEnc(0).value.contains(HttpClient.gzip)) {
      new GZIPInputStream(iStream)
    } else {
      iStream
    }
  }

  private def processResponseHeaders(connWrapper: HttpURLConnection) = {
    val result = new ListBuffer[Header]
    var i = 0
    var key = connWrapper.getHeaderFieldKey(i)
    if (key == null) {
      // some implementations start counting from 1
      i += 1
      key = connWrapper.getHeaderFieldKey(i)
    }
    while (key != null) {
      val value = connWrapper.getHeaderField(i)
      result += Header(key, value)
      i += 1
      key = connWrapper.getHeaderFieldKey(i)
    }
    Headers(result.toList)
  }
}

object HttpClient {
  val defaultCharset = "UTF-8"
  val gzip = "gzip"

  val defaultHeaders = Headers(List())
  //    val defaultHeaders = List(Header(Header.ACCEPT_ENCODING, HttpClient.gzip))

  val defaultResponseBodyFactory = new BufferedBodyFactory

  def terminate() {
    CleanupThread.terminate()
  }
}


/**
 * The cleanup thread exists so that connections do not need to be closed immediately, which improves
 * HTTP1.1 keep-alive performance. The thread receives unclosed connection wrappers and closes them
 * in batches whenever a size limit is reached.
 * <p>
 * The interface to the cleanup thread is via an unbuffered channel (JCSP) that provides all inter-thread
 * synchronisation and thereby keeps the design simple and efficient.
 */
private object CleanupThread extends Thread {
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