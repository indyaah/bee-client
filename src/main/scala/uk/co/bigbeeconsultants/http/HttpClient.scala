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
import request.Request
import java.net._
import java.util.zip._
import org.slf4j.LoggerFactory
import collection.mutable.ListBuffer
import java.io.IOException
import util.DiagnosticTimer

/**
 * Constructs an instance for handling any number of HTTP requests with any level of concurrency.
 *
 * Using this class, cookies must be managed programmatically. If you provide cookie jars, the responses will
 * include cookie jars,which may or may not have been altered by the server. If you do not provide cookie jars,
 * none will come back either.
 *
 * [[uk.co.bigbeeconsultants.http.HttpBrowser]] provides an alternative that handles cookies for you.
 */
class HttpClient(commonConfig: Config = Config()) extends Http(commonConfig) {

  private val logger = LoggerFactory.getLogger(getClass)


  /**
   * Makes an arbitrary request using a response builder. After this call, the response builder will provide the
   * response.
   * @param request the request
   * @param responseBuilder the response factory, e.g. new BufferedResponseBuilder
   * @param config the particular configuration being used for this request; defaults to the commonConfiguration
   *               supplied to this instance of HttpClient
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @throws IllegalStateException if the maximum redirects threshold was exceeded
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
    val timer = new DiagnosticTimer

    val httpURLConnection = configureConnection(openConnection(request, config.proxy), config)

    var redirect: Option[Request] = None
    try {
      config.preRequests.foreach(_.process(request, httpURLConnection, config))
      val remainingCookies = setRequestHeaders(request, httpURLConnection, config)
      httpURLConnection.connect()
      copyRequestBodyToOutputStream(request, httpURLConnection)

      val status = Status(httpURLConnection.getResponseCode, httpURLConnection.getResponseMessage)
      val allResponseHeaders = processResponseHeaders(httpURLConnection)
      val (responseCookies, responseHeadersWithoutCookies) = CookieJar.gleanCookies(remainingCookies, request.url, allResponseHeaders)

      redirect = RedirectionLogic.determineRedirect(config, request, status, responseHeadersWithoutCookies, responseCookies)
      if (redirect.isEmpty) {
        handleContent(httpURLConnection, request, status, responseHeadersWithoutCookies, responseCookies, timer, responseBuilder)
      } else {
        selectStream(httpURLConnection).close()
      }

      if (logger.isDebugEnabled)
        logger.debug(request.toShortString + " -> " + status.code + " " + allResponseHeaders + " " + timer)
      else if (logger.isInfoEnabled)
        logger.info(request.method + " " + request.url + " " + status.code + " " + timer)
    } catch {
      case e: Exception => {
        if (logger.isWarnEnabled)
          logger.warn(request.toShortString + " " + timer + " - " + e.getMessage)
        throw e
      }
    }

    redirect
  }


  @throws(classOf[IOException])
  private def handleContent(httpURLConnection: HttpURLConnection, request: Request, status: Status,
                            responseHeaders: Headers, responseCookies: Option[CookieJar],
                            timer: DiagnosticTimer, responseBuilder: ResponseBuilder) {
    val contEnc = responseHeaders.get(CONTENT_ENCODING)
    val contentType = httpURLConnection.getContentType
    val mediaType = if (contentType != null) Some(MediaType(contentType)) else None
    val stream = if (request.method == Request.HEAD || !status.isBodyAllowed) {
      selectStream(httpURLConnection)
    } else {
      getBodyStream(contEnc, httpURLConnection)
    }
    val delegate = new SelfClosingInputStreamDelegate(stream, httpURLConnection)
    responseBuilder.captureResponse(request, status, mediaType, responseHeaders, responseCookies, delegate, timer)
  }


  /** Provides a seam for testing. Not for normal use. */
  @throws(classOf[IOException])
  protected def openConnection(request: Request, proxy: Option[Proxy]) = {
    if (proxy.isDefined) {
      request.url.openConnection(proxy.get).asInstanceOf[HttpURLConnection]
    } else {
      request.url.openConnection().asInstanceOf[HttpURLConnection]
    }
  }

  private def configureConnection(httpURLConnection: HttpURLConnection, config: Config): HttpURLConnection = {
    httpURLConnection.setAllowUserInteraction(false)
    httpURLConnection.setConnectTimeout(config.connectTimeout)
    httpURLConnection.setReadTimeout(config.readTimeout)
    httpURLConnection.setInstanceFollowRedirects(false)
    httpURLConnection.setUseCaches(config.useCaches)
    httpURLConnection
  }

  private def copyRequestBodyToOutputStream(request: Request, httpURLConnection: HttpURLConnection) {
    if (request.body.isDefined) {
      request.body.get.copyTo(httpURLConnection.getOutputStream)
    }
  }


  private def setRequestHeaders(request: Request, httpURLConnection: HttpURLConnection, config: Config): Option[CookieJar] = {
    val method = request.method.toUpperCase
    httpURLConnection.setRequestMethod(method)

    for (hdr <- request.headers) {
      httpURLConnection.setRequestProperty(hdr.name, hdr.value)
    }

    if (request.body.isDefined) {
      httpURLConnection.setRequestProperty(CONTENT_TYPE, request.body.get.contentType)
    }

    val remainingCookies =
      if (request.cookies.isDefined) {
        val freshJar = request.cookies.get.withoutExpired(new header.HttpDateTimeInstant)
        freshJar.filterForRequest(request.url) match {
          case Some(hdr) =>
            httpURLConnection.setRequestProperty(hdr.name, hdr.value)
          case _ =>
        }
        Some(freshJar)
      } else None

    if (request.body.isDefined) {
      httpURLConnection.setDoOutput(true)
    }

    remainingCookies
  }


  private def getBodyStream(contEnc: Option[Header], httpURLConnection: HttpURLConnection) = {
    val iStream = selectStream(httpURLConnection)
    if (!contEnc.isEmpty) {
      val enc = contEnc.get.toListValue
      if (enc.contains(HttpClient.GZIP)) {
        new GZIPInputStream(iStream)
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

//---------------------------------------------------------------------------------------------------------------------

object HttpClient {
  val UTF8 = "UTF-8"
  val GZIP = "gzip" // only is supported
}

