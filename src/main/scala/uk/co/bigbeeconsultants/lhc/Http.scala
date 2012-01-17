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
import org.apache.commons.io.IOUtils
import java.nio.ByteBuffer
import java.io._
import java.util.zip.GZIPInputStream
import collection.mutable.{ListBuffer, LinkedHashMap}
import org.jcsp.lang.{Any2OneChannel, Channel}

class RequestConfig(val connectTimeout: Int = 2000,
                    val readTimeout: Int = 2000,
                    val followRedirects: Boolean = true,
                    val useCaches: Boolean = true) {
  def setConnectTimeout(newTimeout: Int) = new RequestConfig(newTimeout, readTimeout, followRedirects, useCaches)

  def setReadTimeout(newTimeout: Int) = new RequestConfig(connectTimeout, newTimeout, followRedirects, useCaches)
}


/**
 * Constructs an instance for handling any number of HTTP requests.
 * <p>
 * By default, HTTP cookies are ignored. If you require support for cookies, use the standard
 * java.net.CookieHandler classes, e.g.
 * <code>java.net.CookieHandler.setDefault( new java.net.CookieManager() )</code>.
 *
 * @param keepAlive true for connections to be used once then closed; false for keep-alive connections
 */
final class Http(keepAlive: Boolean = true,
                 requestConfig: RequestConfig = Http.defaultRequestConfig,
                 commonRequestHeaders: List[Header] = Http.defaultHeaders) {

  def head(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request(Request.HEAD, url), requestHeaders)

  def get(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request(Request.GET, url), requestHeaders)

  def delete(url: URL, requestHeaders: List[Header] = Nil) =
    execute(Request(Request.DELETE, url), requestHeaders)

  def post(url: URL, body: Body, requestHeaders: List[Header] = Nil) =
    execute(Request(Request.POST, url, Some(body)), requestHeaders)

  def put(url: URL, body: Body, requestHeaders: List[Header] = Nil) =
    execute(Request(Request.PUT, url, Some(body)), requestHeaders)

  def execute(request: Request, requestHeaders: List[Header] = Nil): Response = {
    val connWrapper = request.url.openConnection.asInstanceOf[HttpURLConnection]
    connWrapper.setAllowUserInteraction(false)
    connWrapper.setConnectTimeout(requestConfig.connectTimeout)
    connWrapper.setReadTimeout(requestConfig.readTimeout);
    connWrapper.setInstanceFollowRedirects(requestConfig.followRedirects)
    connWrapper.setUseCaches(requestConfig.useCaches)

    //useCaches
    //ifModifiedSince

    var result: Response = null
    try {
      setRequestHeaders(request, requestHeaders, connWrapper)

      connWrapper.setDoInput(request.method != Request.HEAD)
      connWrapper.setDoOutput(request.method == Request.POST || request.method == Request.PUT)

      connWrapper.connect()

      if (request.method == Request.POST) writePostData(request.body.get.data, connWrapper.getOutputStream, Http.defaultCharset)
      if (request.method == Request.PUT) IOUtils.write(request.body.get.bytes, connWrapper.getOutputStream)

      result = getContent(connWrapper)
      if (connWrapper.getResponseCode >= 400) {
        throw new RequestException(request, connWrapper.getResponseCode, connWrapper.getResponseMessage, result, null)
      }
    } catch {
      case ioe: IOException =>
        throw new RequestException(request, connWrapper.getResponseCode, connWrapper.getResponseMessage, result, ioe)
    }
    finally {
      markConnectionForClosure(connWrapper)
    }
    result
  }

  private def markConnectionForClosure(connWrapper: HttpURLConnection) {
    if (!keepAlive) connWrapper.disconnect()
    else {
      val cleanupThread = Http.cleanupThread
      cleanupThread.styx.write(Some(connWrapper))
    }
  }

  def closeConnections() {
    if (keepAlive) Http.cleanupThread.styx.write(None)
  }

  private def setRequestHeaders(request: Request, requestHeaders: List[Header], connWrapper: HttpURLConnection) {
    val method = if (request.method == null) Request.GET else request.method.toUpperCase
    connWrapper.setRequestMethod(method)

    val host = request.url.getHost

    if (request.body.isDefined)
      connWrapper.setRequestProperty(Header.CONTENT_TYPE, request.body.get.mediaType.charsetOrElse(Http.defaultCharset))

    for (hdr <- commonRequestHeaders) {
      connWrapper.setRequestProperty(hdr.name, hdr.value.replace(Http.hostPlaceholder, host))
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

  private def getContent(connWrapper: HttpURLConnection): Response = {
    val responseHeaders = processResponseHeaders(connWrapper)
    val contEnc = responseHeaders.get(Header.CONTENT_ENCODING.toUpperCase)
    val stream = if (connWrapper.getResponseCode >= 400) connWrapper.getErrorStream else connWrapper.getInputStream
    val wStream = if (contEnc.isDefined && contEnc.get.value.toLowerCase == Http.gzip) new GZIPInputStream(stream) else stream
    val body = readToByteBuffer(wStream)
    new Response(connWrapper.getResponseCode, connWrapper.getResponseMessage,
      MediaType(connWrapper.getContentType), responseHeaders, body)
  }

  private def readToByteBuffer(inStream: InputStream): ByteBuffer = {
    val bufferSize = 0x20000 // 128K
    val outStream = new ByteArrayOutputStream(bufferSize)
    IOUtils.copy(inStream, outStream)
    ByteBuffer.wrap(outStream.toByteArray)
  }

  private def processResponseHeaders(connWrapper: HttpURLConnection) = {
    val result = new LinkedHashMap[String, Header]
    var i = 0
    var key = connWrapper.getHeaderFieldKey(i)
    while (key != null) {
      val value = connWrapper.getHeaderField(i)
      result(key.toUpperCase) = Header(key, value)
      i += 1
      key = connWrapper.getHeaderFieldKey(i)
    }
    result.toMap
  }
}

object Http {
  //  val charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)")
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
//    Header(Header.ACCEPT_ENCODING, Http.gzip))

  private lazy val cleanupThread = new CleanupThread
}

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

class RequestException(val request: Request, val status: Int, val message: String, val response: Response, cause: Exception)
  extends RuntimeException(cause) {

  //  override def getMessage: String = {
  //    "RequestException\n%s to %s\nStatus %s %s\nBody: %s".format(method, url, status, message, response.body)
  //  }
}
