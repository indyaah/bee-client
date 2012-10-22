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
import header._
import response._
import request.{RequestBody, Request}
import java.net._
import java.util.zip.GZIPInputStream
import com.weiglewilczek.slf4s.Logging
import collection.mutable.ListBuffer
import java.io.IOException
import javax.net.ssl.HttpsURLConnection
import scala.Some

/**
 * Constructs an instance for handling any number of HTTP requests with any level of concurrency.
 *
 * Using this class, cookies must be managed programmatically. If you provide cookie jars, the responses will
 * include cookie jars,which may or may not have been altered by the server. If you do not provide cookie jars,
 * none will come back either.
 *
 * [[uk.co.bigbeeconsultants.http.HttpBrowser]] provides an alternative that handles cookies for you.
 */
class HttpClient(val commonConfig: Config = Config()) extends Logging {

  /**
   * Make a HEAD request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def head(url: URL, requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.head(url) + requestHeaders)
  }

  /**
   * Make a HEAD request with cookies.
   */
  @throws(classOf[IOException])
  def head(url: URL, requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.head(url) + requestHeaders using jar)
  }


  /**
   * Make a TRACE request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def trace(url: URL, requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.trace(url) + requestHeaders)
  }

  /**
   * Make a TRACE request with cookies.
   */
  @throws(classOf[IOException])
  def trace(url: URL, requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.trace(url) + requestHeaders using jar)
  }


  /**
   * Make a GET request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def get(url: URL, requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.get(url) + requestHeaders)
  }

  /**
   * Make a GET request with cookies.
   */
  @throws(classOf[IOException])
  def get(url: URL, requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.get(url) + requestHeaders using jar)
  }


  /**
   * Make a DELETE request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def delete(url: URL, requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.delete(url) + requestHeaders)
  }

  /**
   * Make a DELETE request with cookies.
   */
  @throws(classOf[IOException])
  def delete(url: URL, requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.delete(url) + requestHeaders using jar)
  }


  /**
   * Make an OPTIONS request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def options(url: URL, body: Option[RequestBody], requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.options(url, body) + requestHeaders)
  }

  /**
   * Make an OPTIONS request with cookies.
   */
  @throws(classOf[IOException])
  def options(url: URL, body: Option[RequestBody], requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.options(url, body) + requestHeaders using jar)
  }


  /**
   * Make a POST request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def post(url: URL, body: Option[RequestBody], requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.post(url, body) + requestHeaders)
  }

  /**
   * Make a POST request with cookies.
   */
  @throws(classOf[IOException])
  def post(url: URL, body: Option[RequestBody], requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.post(url, body) + requestHeaders using jar)
  }


  /**
   * Make a PUT request. No cookies are used and none are returned.
   */
  @throws(classOf[IOException])
  def put(url: URL, body: RequestBody, requestHeaders: Headers = Nil): Response = {
    makeRequest(Request.put(url, body) + requestHeaders)
  }

  /**
   * Make a PUT request with cookies.
   */
  @throws(classOf[IOException])
  def put(url: URL, body: RequestBody, requestHeaders: Headers, jar: CookieJar): Response = {
    makeRequest(Request.put(url, body) + requestHeaders using jar)
  }


  /**
   * Makes an arbitrary request and returns the response. The entire response body is read into memory.
   * @param request the request
   * @param config the particular configuration being used for this request; defaults to the commonConfiguration
   *               supplied to this instance of HttpClient
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @return the response (for all outcomes including 4xx and 5xx status codes) if
   *         no exception occurred
   */
  @throws(classOf[IOException])
  def makeRequest(request: Request, config: Config = commonConfig): Response = {
    val responseBuilder = new BufferedResponseBuilder
    execute(request, responseBuilder, config)
    responseBuilder.response.get
  }


  /**
   * Makes an arbitrary request using a response builder and without following any redirection.
   * @param request the request
   * @param responseBuilder the response factory, e.g. new BufferedResponseBuilder
   * @param config the particular configuration being used for this request; defaults to the commonConfiguration
   *               supplied to this instance of HttpClient
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @throws IllegalStateException if the maximum redirects threshold was exceeded
   * @return the response (for all outcomes including 4xx and 5xx status codes) if
   *         no exception occurred
   */
  @throws(classOf[IOException])
  @throws(classOf[IllegalStateException])
  def execute(request: Request, responseBuilder: ResponseBuilder, config: Config = commonConfig) {
    RedirectionLogic.doExecute(this, request, responseBuilder, config)
  }


  @throws(classOf[IOException])
  private[http] def doExecute(request: Request,
                              responseBuilder: ResponseBuilder,
                              config: Config): Option[Request] = {
    logger.info({
      request.toString
    })

    val httpURLConnection = openConnection(request, config.proxy)
    httpURLConnection.setAllowUserInteraction(false)
    httpURLConnection.setConnectTimeout(config.connectTimeout)
    httpURLConnection.setReadTimeout(config.readTimeout)
    httpURLConnection.setInstanceFollowRedirects(false)
    httpURLConnection.setUseCaches(config.useCaches)

    if (httpURLConnection.isInstanceOf[HttpsURLConnection]) {
      val httpsConnection = httpURLConnection.asInstanceOf[HttpsURLConnection]
      if (config.sslSocketFactory.isDefined)
        httpsConnection.setSSLSocketFactory(config.sslSocketFactory.get)
      if (config.hostnameVerifier.isDefined)
        httpsConnection.setHostnameVerifier(config.hostnameVerifier.get)
    }

    var redirect: Option[Request] = None
    try {
      setRequestHeaders(request, httpURLConnection, config)
      httpURLConnection.connect()
      copyRequestBodyToOutputStream(request, httpURLConnection)

      val status = Status(httpURLConnection.getResponseCode, httpURLConnection.getResponseMessage)
      val allResponseHeaders = processResponseHeaders(httpURLConnection)
      val (responseCookies, responseHeadersWithoutCookies) =
        if (request.cookies.isDefined)
          request.cookies.get.gleanCookies(request.url, allResponseHeaders)
        else
          (None, allResponseHeaders)

      redirect = RedirectionLogic.determineRedirect(config, request, status, responseHeadersWithoutCookies, responseCookies)
      if (redirect.isEmpty) {
        handleContent(httpURLConnection, request, status, responseHeadersWithoutCookies, responseCookies, responseBuilder)
      }
    } finally {
      httpURLConnection.disconnect()
    }

    redirect
  }


  @throws(classOf[IOException])
  private def handleContent(httpURLConnection: HttpURLConnection, request: Request, status: Status,
                            responseHeaders: Headers, responseCookies: Option[CookieJar], responseBuilder: ResponseBuilder) {
    val contEnc = responseHeaders.get(CONTENT_ENCODING)
    val contentType = httpURLConnection.getContentType
    val mediaType = if (contentType != null) Some(MediaType(contentType)) else None
    val stream = if (request.method == Request.HEAD || !status.isBodyAllowed) {
      selectStream(httpURLConnection)
    } else {
      getBodyStream(contEnc, httpURLConnection)
    }
    responseBuilder.captureResponse(request, status, mediaType, responseHeaders, responseCookies, stream)
  }


  /** Provides a seam for testing. Not for normal use. */
  @throws(classOf[IOException])
  protected def openConnection(request: Request, proxy: Proxy) = request.url.openConnection(proxy).asInstanceOf[HttpURLConnection]

  private def copyRequestBodyToOutputStream(request: Request, httpURLConnection: HttpURLConnection) {
    if (request.body.isDefined) {
      request.body.get.copyTo(httpURLConnection.getOutputStream)
    }
  }


  private def setRequestHeaders(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    val method = request.method.toUpperCase
    httpURLConnection.setRequestMethod(method)

    config.preRequests.foreach(_.process(request, httpURLConnection, config))

    if (request.body.isDefined) {
      httpURLConnection.setRequestProperty(CONTENT_TYPE, request.body.get.mediaType)
      logger.debug((CONTENT_TYPE -> request.body.get.mediaType).toString)
    }

    for (hdr <- request.headers) {
      httpURLConnection.setRequestProperty(hdr.name, hdr.value)
      logger.debug(hdr.toString)
    }

    if (request.cookies.isDefined) {
      request.cookies.get.filterForRequest(request.url) match {
        case Some(hdr) =>
          httpURLConnection.setRequestProperty(hdr.name, hdr.value)
          logger.debug(hdr.toString)
        case _ =>
      }
    }

    if (request.body.isDefined) {
      httpURLConnection.setDoOutput(true)
    }
  }


  private def getBodyStream(contEnc: Option[Header], httpURLConnection: HttpURLConnection) = {
    val iStream = selectStream(httpURLConnection)
    if (!contEnc.isEmpty) {
      val enc = contEnc.get.toQualifiedValue
      if (enc.parts.exists(_.value == HttpClient.GZIP)) {
        new GZIPInputStream(iStream)
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
    var key = urlConnection.getHeaderFieldKey(i)
    if (key == null) {
      // some implementations start counting from 1
      i += 1
      key = urlConnection.getHeaderFieldKey(i)
    }
    while (key != null) {
      val value = urlConnection.getHeaderField(i)
      result += Header(key, value)
      i += 1
      key = urlConnection.getHeaderFieldKey(i)
    }
    Headers(result.toList)
  }
}

object HttpClient {
  val UTF8 = "UTF-8"
  val GZIP = "gzip"
  //val DEFLATE = "deflate"
}

