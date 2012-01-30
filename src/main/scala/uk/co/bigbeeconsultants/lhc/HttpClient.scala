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

import java.lang.String
import java.net.{URLEncoder, URL, HttpURLConnection}
import java.nio.ByteBuffer
import java.io._
import java.util.zip.GZIPInputStream
import collection.mutable.{ListBuffer, LinkedHashMap}
import org.jcsp.lang.{Any2OneChannel, Channel}

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
                       requestConfig: RequestConfig = HttpClient.defaultRequestConfig,
                       commonRequestHeaders: List[Header] = HttpClient.defaultHeaders) {
  private val emptyBuffer = ByteBuffer.allocateDirect(0)

  def head(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request.head(url), requestHeaders)

  def trace(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request.trace(url), requestHeaders)

  def get(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request.get(url), requestHeaders)

  def delete(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request.delete(url), requestHeaders)

  def options(url: URL, body: Option[Body], requestHeaders: List[Header] = Nil) =
    execute(Request.options(url, body), requestHeaders)

  def post(url: URL, body: Body, requestHeaders: List[Header] = Nil) =
    execute(Request.post(url, body), requestHeaders)

  def put(url: URL, body: Body, requestHeaders: List[Header] = Nil) =
    execute(Request.put(url, body), requestHeaders)

  def execute(request: Request, requestHeaders: List[Header] = Nil): Response = {
    val connWrapper = request.url.openConnection.asInstanceOf[HttpURLConnection]
    connWrapper.setAllowUserInteraction(false)
    connWrapper.setConnectTimeout(requestConfig.connectTimeout)
    connWrapper.setReadTimeout(requestConfig.readTimeout);
    connWrapper.setInstanceFollowRedirects(requestConfig.followRedirects)
    connWrapper.setUseCaches(requestConfig.useCaches)

    //useCaches
    //ifModifiedSince

    try {
      setRequestHeaders(request, requestHeaders, connWrapper)

      //      connWrapper.setDoInput(request.method != Request.HEAD)
      connWrapper.setDoOutput(request.method == Request.POST || request.method == Request.PUT)

      connWrapper.connect()

      copyRequestBodyToOutputStream(request, connWrapper)

      val result = getContent(request, connWrapper)
      if (connWrapper.getResponseCode >= 400) {
        throw new RequestException(request, connWrapper.getResponseCode, connWrapper.getResponseMessage, result, null)
      }
      result

    } catch {
      case ioe: IOException =>
        throw new RequestException(request, connWrapper.getResponseCode, connWrapper.getResponseMessage, null, ioe)

    } finally {
      markConnectionForClosure(connWrapper)
    }
  }

  private def copyRequestBodyToOutputStream(request: Request, connWrapper: HttpURLConnection) {
    if (request.method == Request.POST || request.method == Request.PUT) {
      require(request.body.isDefined, "An entity body is required when making a POST request.")
      if (request.method == Request.POST) {
        if (request.body.get.data.isRight) {
          writePostData(request.body.get.data.right.get, connWrapper.getOutputStream, HttpClient.defaultCharset)
        }
        else {
          connWrapper.getOutputStream.write(request.body.get.data.left.get)
        }
      }
    }
  }

  private def markConnectionForClosure(connWrapper: HttpURLConnection) {
    if (!keepAlive) connWrapper.disconnect()
    else {
      val cleanupThread = HttpClient.cleanupThread
      cleanupThread.styx.write(Some(connWrapper))
    }
  }

  def closeConnections() {
    if (keepAlive) HttpClient.cleanupThread.styx.write(None)
  }

  private def setRequestHeaders(request: Request, requestHeaders: List[Header], connWrapper: HttpURLConnection) {
    val method = if (request.method == null) Request.GET else request.method.toUpperCase
    connWrapper.setRequestMethod(method)

    val host = request.url.getHost

    if (request.body.isDefined)
      connWrapper.setRequestProperty(Header.CONTENT_TYPE, request.body.get.mediaType.toString)
    //.charsetOrElse(HttpClient.defaultCharset.toLowerCase))

    for (hdr <- commonRequestHeaders) {
      connWrapper.setRequestProperty(hdr.name, hdr.value.replace(HttpClient.hostPlaceholder, host))
    }

    for (hdr <- requestHeaders) {
      connWrapper.setRequestProperty(hdr.name, hdr.value)
    }
  }

  private def writePostData(data: List[KeyVal], outputStream: OutputStream, encoding: String) {
    val w = new OutputStreamWriter(outputStream, encoding)
    var first = true
    for (keyVal <- data) {
      if (!first) w.append('&')
      else first = false
      w.write(URLEncoder.encode(keyVal.key, encoding))
      w.write('=')
      w.write(URLEncoder.encode(keyVal.value, encoding))
    }
    w.close()
  }

  private def getContent(request: Request, connWrapper: HttpURLConnection): Response = {
    val responseHeaders = processResponseHeaders(connWrapper)
    if (request.method == Request.HEAD) {
      val status = Status(connWrapper.getResponseCode, connWrapper.getResponseMessage)
      val mediaType = MediaType(connWrapper.getContentType)
      Response(status, mediaType, responseHeaders, emptyBuffer)

    } else {
      val contEnc = responseHeaders.get(Header.CONTENT_ENCODING.toUpperCase)
      val stream = if (connWrapper.getResponseCode >= 400) connWrapper.getErrorStream else connWrapper.getInputStream
      val wStream = if (contEnc.isDefined && contEnc.get.value.toLowerCase == HttpClient.gzip) new GZIPInputStream(stream) else stream
      val body = readToByteBuffer(wStream)
      Response(Status(connWrapper.getResponseCode, connWrapper.getResponseMessage),
        MediaType(connWrapper.getContentType), responseHeaders, body)
    }
  }

  private def readToByteBuffer(inStream: InputStream): ByteBuffer = {
    val bufferSize = 0x20000 // 128K
    val outStream = new ByteArrayOutputStream(bufferSize)
    Util.copyBytes(inStream, outStream)
    ByteBuffer.wrap(outStream.toByteArray)
  }

  private def processResponseHeaders(connWrapper: HttpURLConnection) = {
    val result = new LinkedHashMap[String, Header]
    var i = 0
    var key = connWrapper.getHeaderFieldKey(i)
    if (key == null) {
      // some implementations start counting from 1
      i += 1
      key = connWrapper.getHeaderFieldKey(i)
    }
    while (key != null) {
      val value = connWrapper.getHeaderField(i)
      result(key.toUpperCase) = Header(key, value)
      i += 1
      key = connWrapper.getHeaderFieldKey(i)
    }
    result.toMap
  }
}

object HttpClient {
  val defaultCharset = "UTF-8"
  val gzip = "gzip"

  /**
   * Use this in any header wherever the hostname part of the URL is required.
   */
  val hostPlaceholder = "{HOST}"

  /**
   * The default request configuration has two-second timeouts, follows redirects and allows gzip compression.
   */
  val defaultRequestConfig = new RequestConfig

  val defaultHeaders = List(
    Header(Header.HOST, hostPlaceholder))
  //    Header(Header.ACCEPT_ENCODING, HttpClient.gzip))

  private lazy val cleanupThread = new CleanupThread
}

/**
 * The cleanup thread exists so that connections do not need to be closed immediately, which improves
 * HTTP1.1 keep-alive performance. The thread receives unclosed connection wrappers and closes them
 * in batches whenever a size limit is reached.
 * <p>
 * The interface to the cleanup thread is via an unbuffered channel (JCSP) that provides all inter-thread
 * synchronisation and thereby keeps the design simple and efficient.
 */
private class CleanupThread extends Thread {
  private val channel: Any2OneChannel[Option[HttpURLConnection]] = Channel.any2one()
  val styx = channel.out

  setName("httpCleanup")
  setDaemon(true)
  start()

  val limit = 1000
  private val zombies = new ListBuffer[HttpURLConnection]

  override def run() {
    while (true) {
      channel.in.read match {
        case Some(connWrapper) =>
          zombies += connWrapper
          if (zombies.size > limit) cleanup()
        case None =>
          cleanup()
      }
    }
  }

  private def cleanup() {
    for (conn <- zombies) conn.disconnect()
    zombies.clear()
  }
}
