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

import header.HeaderName._
import header.{CookieJar, Headers, Header, MediaType}
import response._
import request.{Config, RequestBody, Request}
import java.net._
import java.util.zip.GZIPInputStream
import com.weiglewilczek.slf4s.Logging
import collection.mutable.ListBuffer
import collection.immutable.List
import java.io.IOException

/**
 * Constructs an instance for handling any number of HTTP requests.
 */
class HttpClient(val config: Config = Config (),
                 val commonRequestHeaders: Headers = HttpClient.defaultRequestHeaders,
                 val proxy: Proxy = Proxy.NO_PROXY) extends Logging {

  /**
   * Make a HEAD request.
   */
  @throws(classOf[IOException])
  def head(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.head (url), requestHeaders, jar)
  }

  /**
   * Make a TRACE request.
   */
  @throws(classOf[IOException])
  def trace(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.trace (url), requestHeaders, jar)
  }

  /**
   * Make a GET request.
   */
  @throws(classOf[IOException])
  def get(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.get (url), requestHeaders, jar)
  }

  /**
   * Make a DELETE request.
   */
  @throws(classOf[IOException])
  def delete(url: URL, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.delete (url), requestHeaders, jar)
  }

  /**
   * Make an OPTIONS request.
   */
  @throws(classOf[IOException])
  def options(url: URL, body: Option[RequestBody], requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.options (url, body), requestHeaders, jar)
  }

  /**
   * Make a POST request.
   */
  @throws(classOf[IOException])
  def post(url: URL, body: Option[RequestBody], requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.post (url, body), requestHeaders, jar)
  }

  /**
   * Make a PUT request.
   */
  @throws(classOf[IOException])
  def put(url: URL, body: RequestBody, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    execute (Request.put (url, body), requestHeaders, jar)
  }

  /**
   * Makes an arbitrary request and returns the response. The entire response body is read into memory.
   * @param request the request
   * @param requestHeaders the optional request headers (use Nil if none are required)
   * @param jar the optional cookie jar (use CookieJar.empty if none is required)
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @return the response (for all outcomes including 4xx and 5xx status codes) if
   *         no exception occurred
   */
  @throws(classOf[IOException])
  def execute(request: Request, requestHeaders: Headers = Nil, jar: CookieJar = CookieJar.empty): Response = {
    val responseFactory = new BufferedResponseBuilder
    execute (request, requestHeaders, jar, responseFactory)
    responseFactory.response.get
  }

  /**
   * Makes an arbitrary request using a response builder.
   * @param request the request
   * @param requestHeaders the optional request headers (use Nil if none are required)
   * @param jar the optional cookie jar (use CookieJar.empty if none is required)
   * @param responseFactory the response factory, e.g. new BufferedResponseBuilder
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @return the response (for all outcomes including 4xx and 5xx status codes) if
   *         no exception occurred
   */
  @throws(classOf[IOException])
  def execute(request: Request, requestHeaders: Headers, jar: CookieJar, responseFactory: ResponseBuilder) {
    logger.debug (request.toString)

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
      handleContent (status, request, responseFactory, httpURLConnection)

    } finally {
      markConnectionForClosure (httpURLConnection)
    }
  }

  /**Provides a seam for testing. Not for normal use. */
  @throws(classOf[IOException])
  protected def openConnection(request: Request) = request.url.openConnection (proxy).asInstanceOf[HttpURLConnection]

  private def copyRequestBodyToOutputStream(request: Request, httpURLConnection: HttpURLConnection) {
    if (request.body.isDefined) {
      request.body.get.copyTo (httpURLConnection.getOutputStream)
    }
  }

  private def markConnectionForClosure(httpURLConnection: HttpURLConnection) {
    //    if (!config.keepAlive) {
    httpURLConnection.disconnect ()
    //    }
    //    else CleanupThread.futureClose(httpURLConnection)
  }

  private def setRequestHeaders(request: Request, requestHeaders: Headers, jar: CookieJar, httpURLConnection: HttpURLConnection) {
    val method = if (request.method == null) Request.GET else request.method.toUpperCase
    httpURLConnection.setRequestMethod (method)

    if (config.sendHostHeader) {
      httpURLConnection.setRequestProperty (HOST, request.url.getHost)
      logger.debug ((HOST -> request.url.getHost).toString)
    }

    if (request.body.isDefined) {
      httpURLConnection.setRequestProperty (CONTENT_TYPE, request.body.get.mediaType)
      logger.debug ((CONTENT_TYPE -> request.body.get.mediaType).toString)
    }

    for (hdr <- commonRequestHeaders.list) {
      httpURLConnection.setRequestProperty (hdr.name, hdr.value)
      logger.debug (hdr.toString)
    }

    for (hdr <- requestHeaders.list) {
      httpURLConnection.setRequestProperty (hdr.name, hdr.value)
      logger.debug (hdr.toString)
    }

    jar.filterForRequest (request.url) match {
      case Some (hdr) =>
        httpURLConnection.setRequestProperty (hdr.name, hdr.value)
        logger.debug (hdr.toString)
      case _ =>
    }

    if (request.body.isDefined) {
      httpURLConnection.setDoOutput (true)
    }
  }

  private def handleContent(status: Status, request: Request, responseFactory: ResponseBuilder, httpURLConnection: HttpURLConnection) {
    val responseHeaders = processResponseHeaders (httpURLConnection)
    val contEnc = responseHeaders.get (CONTENT_ENCODING)
    val contentType = httpURLConnection.getContentType
    val mediaType = if (contentType != null) Some (MediaType (contentType)) else None

    if (request.method == Request.HEAD || status.category == 1 ||
      status.code == Status.S2_NO_CONTENT || status.code == Status.S3_NOT_MODIFIED) {
      val iStream = selectStream (httpURLConnection)
      responseFactory.captureResponse (request, status, mediaType, responseHeaders, iStream)

    } else {
      val stream = getBodyStream (contEnc, httpURLConnection)
      responseFactory.captureResponse (request, status, mediaType, responseHeaders, stream)
    }
  }

  private def getBodyStream(contEnc: Option[Header], httpURLConnection: HttpURLConnection) = {
    val iStream = selectStream (httpURLConnection)
    if (!contEnc.isEmpty) {
      val enc = contEnc.get.toQualifiedValue
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

  /**
   * Closes any remaining connections in this HttpClient instance. Use this to clean up. It is possible to continue
   * to make further connections after each invocation of this method.
   */
  def closeConnections() {
    //    if (config.keepAlive) CleanupThread.closeConnections()
  }
}

object HttpClient {
  val UTF8 = "UTF-8"
  val GZIP = "gzip"
  val DEFLATE = "deflate"

  val defaultRequestHeaders = Headers (
    ACCEPT_ENCODING -> GZIP,
    // CONNECTION -> "Close",
    ACCEPT_CHARSET -> UTF8
  )

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

