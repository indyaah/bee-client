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
import java.net.{URLEncoder, URLConnection, URL, HttpURLConnection}
import org.apache.commons.io.IOUtils
import java.nio.ByteBuffer
import java.io._
import java.util.zip.GZIPInputStream
import collection.mutable.{ListBuffer, LinkedHashMap}

case class RequestConfig(connectTimeout: Int,
                         readTimeout: Int,
                         followRedirects: Boolean,
                         commonRequestHeaders: List[Header] = Nil)

class Http(autoClose: Boolean = true) {
  URLConnection.setDefaultAllowUserInteraction(false)

  /**
   * The default request configuration has two-second timeouts, follows redirects and allows gzip compression.
   */
  val defaultRequestConfig = RequestConfig(2000, 2000, true, List(Header(Header.ACCEPT_ENCODING, Http.gzip)))

  /**
   * Set the request configuration as needed.
   */
  var requestConfig = defaultRequestConfig

  // TODO thread-safe data structure is needed here
  private val unclosedConnections = new ListBuffer[HttpURLConnection]

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
    connWrapper.setConnectTimeout(requestConfig.connectTimeout)
    connWrapper.setReadTimeout(requestConfig.readTimeout);
    connWrapper.setInstanceFollowRedirects(requestConfig.followRedirects)
    //    if (req.cookies().size() > 0)
    //      connWrapper.addRequestProperty(Header.COOKIE, getRequestCookieString(req));

    var result: Response = null
    try {
      setRequestHeaders(request, requestHeaders, connWrapper)

      if (request.method == Request.POST || request.method == Request.PUT) connWrapper.setDoOutput(true)

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
      if (autoClose) connWrapper.disconnect()
      else addUnclosedConnection(connWrapper)
    }
    result
  }

  private def addUnclosedConnection(connWrapper: HttpURLConnection) {
    // keep a lid on memory footprint
    unclosedConnections += connWrapper
    if (unclosedConnections.size > 1000) closeConnections()
  }

  def closeConnections() {
    // TODO thread-safe iteration needed
    for (conn <- unclosedConnections) {
      conn.disconnect()
    }
    unclosedConnections.clear()
  }

  private def setRequestHeaders(request: Request, requestHeaders: List[Header], connWrapper: HttpURLConnection) {
    val method = if (request.method == null) Request.GET else request.method.toUpperCase
    connWrapper.setRequestMethod(method)
    connWrapper.setRequestProperty(Header.HOST, request.url.getHost)
    if (request.body.isDefined) connWrapper.setRequestProperty(Header.CONTENT_ENCODING, request.body.get.mediaType.charsetOrElse(Http.defaultCharset))
    for (hdr <- requestConfig.commonRequestHeaders) {
      connWrapper.setRequestProperty(hdr.name, hdr.value)
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
    new Response(connWrapper.getResponseCode, connWrapper.getResponseMessage, connWrapper.getContentEncoding,
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
}

class RequestException(val request: Request, val status: Int, val message: String, val response: Response, cause: Exception)
  extends RuntimeException(cause) {

  //  override def getMessage: String = {
  //    "RequestException\n%s to %s\nStatus %s %s\nBody: %s".format(method, url, status, message, response.body)
  //  }
}
