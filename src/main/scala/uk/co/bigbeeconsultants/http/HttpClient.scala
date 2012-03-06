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

package uk.co.bigbeeconsultants.http

import header.{CookieJar, HeaderName, Headers, Header, MediaType}
import response._
import request.{Config, RequestException, Body, Request}
import java.io._
import java.net.{URLConnection, URL, Proxy, HttpURLConnection}
import collection.mutable.ListBuffer
import HeaderName._
import Status._
import java.util.zip.GZIPInputStream

/**
 * Constructs an instance for handling any number of HTTP requests.
 */
class HttpClient(val config: Config = Config (),
                 val commonRequestHeaders: Headers = HttpClient.defaultRequestHeaders,
                 val responseBodyFactory: BodyFactory = HttpClient.defaultResponseBodyFactory,
                 val proxy: Proxy = Proxy.NO_PROXY) {

  /**
   * Make a HEAD request.
   */
  def head(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.head (url), requestHeaders, jar)

  /**
   * Make a TRACE request.
   */
  def trace(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.trace (url), requestHeaders, jar)

  /**
   * Make a GET request.
   */
  def get(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.get (url), requestHeaders, jar)

  /**
   * Make a DELETE request.
   */
  def delete(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.delete (url), requestHeaders, jar)

  /**
   * Make an OPTIONS request.
   */
  def options(url: URL, body: Option[Body], requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.options (url, body), requestHeaders, jar)

  /**
   * Make a POST request.
   */
  def post(url: URL, body: Body, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.post (url, body), requestHeaders, jar)

  /**
   * Make a PUT request.
   */
  def put(url: URL, body: Body, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty) =
    execute (Request.put (url, body), requestHeaders, jar)

  /**
   * Make an arbitrary request.
   */
  def execute(request: Request, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    val httpURLConnection = openConnection (request)
    httpURLConnection.setAllowUserInteraction (false)
    httpURLConnection.setConnectTimeout (config.connectTimeout)
    httpURLConnection.setReadTimeout (config.readTimeout);
    httpURLConnection.setInstanceFollowRedirects (config.followRedirects)
    httpURLConnection.setUseCaches (config.useCaches)

    try {
      setRequestHeaders (request, requestHeaders, jar, httpURLConnection)

      httpURLConnection.connect ()

      copyRequestBodyToOutputStream (request, httpURLConnection)

      val status = Status (httpURLConnection.getResponseCode, httpURLConnection.getResponseMessage)
      val result = getContent (status, request, httpURLConnection)
      if (httpURLConnection.getResponseCode >= 400) {
        throw new RequestException (request, status, Some (result), None)
      }
      result

    } catch {
      case ioe: IOException =>
        val status = Status (S5_INTERNAL_ERROR, ioe.getMessage)
        throw new RequestException (request, status, None, Some (ioe))

    } finally {
      markConnectionForClosure (httpURLConnection)
    }
  }

  /**Provides a seam for testing. Not for normal use. */
  protected def openConnection(request: Request) = request.url.openConnection (proxy).asInstanceOf[HttpURLConnection]

  private def copyRequestBodyToOutputStream(request: Request, httpURLConnection: HttpURLConnection) {
    if (request.method == Request.POST || request.method == Request.PUT) {
      require (request.body.isDefined, "An entity body is required when making a POST request.")
      request.body.get.copyTo (httpURLConnection.getOutputStream)
    }
  }

  private def markConnectionForClosure(httpURLConnection: HttpURLConnection) {
    //    if (!config.keepAlive) {
    httpURLConnection.disconnect ()
    //    }
    //    else CleanupThread.futureClose(httpURLConnection)
  }

  def closeConnections() {
    //    if (config.keepAlive) CleanupThread.closeConnections()
  }

  private def setRequestHeaders(request: Request, requestHeaders: Headers, jar: CookieJar, httpURLConnection: HttpURLConnection) {
    val method = if (request.method == null) Request.GET else request.method.toUpperCase
    httpURLConnection.setRequestMethod (method)

    if (config.sendHostHeader) {
      httpURLConnection.setRequestProperty (HOST, request.url.getHost)
    }

    if (request.body.isDefined) {
      httpURLConnection.setRequestProperty (CONTENT_TYPE, request.body.get.mediaType)
    }

    for (hdr <- commonRequestHeaders.list) {
      httpURLConnection.setRequestProperty (hdr.name, hdr.value)
    }

    for (hdr <- requestHeaders.list) {
      httpURLConnection.setRequestProperty (hdr.name, hdr.value)
    }

    jar.filterForRequest (request.url) match {
      case Some (hdr) => httpURLConnection.setRequestProperty (hdr.name, hdr.value)
      case _ =>
    }

    if (request.method == Request.POST || request.method == Request.PUT) {
      httpURLConnection.setDoOutput (true)
      //      if (config.chunkSizeInKB >= 0) {
      //        httpURLConnection.setFixedLengthStreamingMode(config.chunkSizeInKB * 1024)
      //      }
    }
  }

  private def getContent(status: Status, request: Request, httpURLConnection: HttpURLConnection): Response = {
    val responseHeaders = processResponseHeaders (httpURLConnection)
    val contEnc = responseHeaders.find (CONTENT_ENCODING)
    val mediaType = MediaType (httpURLConnection.getContentType)
    val body = responseBodyFactory.newBody (mediaType)

    if (request.method == Request.HEAD || status.category == 1 ||
      status.code == Status.S2_NO_CONTENT || status.code == Status.S3_NOT_MODIFIED) {
      val iStream = selectStream (httpURLConnection)
      //      Response (request, status, new EmptyBody (mediaType), responseHeaders)
      body.receiveData (mediaType, iStream)
      Response (request, status, body, responseHeaders)

    } else {
      val stream = getBodyStream (contEnc, httpURLConnection)
      body.receiveData (mediaType, stream)
      Response (request, status, body, responseHeaders)
    }
  }

  private def getBodyStream(contEnc: List[Header], httpURLConnection: HttpURLConnection) = {
    val iStream = selectStream (httpURLConnection)
    if (!contEnc.isEmpty) {
      val enc = contEnc (0).toQualifiedValue
      if (enc.parts.exists (_.value == HttpClient.GZIP)) {
        new GZIPInputStream (iStream)
        //TODO deflate
        //      } else if (enc.contains (HttpClient.DEFLATE)) {
        //        new InflaterInputStream (iStream, new Inflater (true))
      } else {
        iStream
      }
    } else {
      iStream
    }
  }

  private def selectStream(httpURLConnection: HttpURLConnection) = {
    if (httpURLConnection.getResponseCode >= 400)
      httpURLConnection.getErrorStream
    else
      httpURLConnection.getInputStream
  }

  private def processResponseHeaders(urlConnection: URLConnection) = {
    val result = new ListBuffer[Header]
    var i = 0
    var key = urlConnection.getHeaderFieldKey (i)
    if (key == null) {
      // some implementations start counting from 1
      i += 1
      key = urlConnection.getHeaderFieldKey (i)
    }
    while (key != null) {
      val value = urlConnection.getHeaderField (i)
      result += Header (key, value)
      i += 1
      key = urlConnection.getHeaderFieldKey (i)
    }
    Headers (result.toList)
  }
}

object HttpClient {
  val UTF8 = "UTF-8"
  val GZIP = "gzip"
  val DEFLATE = "deflate"

  val defaultRequestHeaders = Headers (List (
    Header (ACCEPT_ENCODING, GZIP),
    //    Header (CONNECTION, "Close"),
    Header (ACCEPT_CHARSET, UTF8)
  ))

  /**
   * Provides the 'standard' way to capture response bodies in buffers that
   * are easily converted to strings.
   */
  val defaultResponseBodyFactory = new BufferedBodyFactory

  /**
   * Closes all the keep-alive connections still pending.
   */
  def closeConnections() {
    CleanupThread.closeConnections ()
  }

  // Shuts down the background cleanup thread. Do not call this more than once.
  def terminate() {
    CleanupThread.terminate ()
  }
}

