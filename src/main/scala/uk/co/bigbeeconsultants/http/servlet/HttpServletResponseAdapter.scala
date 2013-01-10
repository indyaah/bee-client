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

package uk.co.bigbeeconsultants.http.servlet

import javax.servlet.http.HttpServletResponse
import uk.co.bigbeeconsultants.http._
import header._
import header.HeaderName._
import response.BufferedResponseBuilder
import util.HttpUtil._
import java.io.IOException

/**
 * Adapts HTTP Servlet response objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily. Outline usage:
 * {{{
 *   val adapter = new HttpServletResponseAdapter(httpServletResponse)
 *   httpClient.execute(request, adapter.responseBuilder)
 *   // optionally use adapter.response
 *   // optionally: adapter.setResponseHeaders(...)
 *   // optionally: adapter.setResponseCookies(...)
 *   adapter.sendResponse()
 * }}}
 * The `adapter.response` is not available until *after* executing the http request; a NoSuchElementException will
 * occur if it is accessed beforehand.
 * @param resp the response to be created
 * @param textualBodyFilter an optional mutation that may be applied to every line of the body content.
 */
class HttpServletResponseAdapter(resp: HttpServletResponse,
                                 textualBodyFilter: Option[TextualBodyFilter] = None) {

  /** Provides the means to capture the response from the downstream server. */
  val responseBuilder = new BufferedResponseBuilder

  /**
   * Provides access to the response from the downstream server. This is not available until the request
   * has been executed, which must be handled by the calling code.
   */
  lazy val response = responseBuilder.response.get

  /** Optionally, sets extra response headers before sending the response. */
  def setResponseHeaders(headers: Headers) {
    for (header <- headers.list) {
      val value =
        if (header.name == LOCATION.name && textualBodyFilter.isDefined) textualBodyFilter.get.lineProcessor(header.value)
        else header.value
      resp.setHeader(header.name, value)
    }
  }

  /** Optionally, sets extra response cookies before sending the response. */
  def setResponseCookies(jar: CookieJar) {
    for (cookie <- jar.cookies) {
      resp.addCookie(cookie.asServletCookie)
    }
  }

  /** Sends the response back to the client via the `HttpServletResponse`. */
  @throws(classOf[IOException])
  def sendResponse() {
    val status = response.status
    val body = response.body
    val headers = response.headers
    val cookies = response.cookies

    try {
      resp.setStatus(status.code, status.message)

      setResponseHeaders(headers)

      if (cookies.isDefined) {
        setResponseCookies(cookies.get)
      }

      if (textualBodyFilter.isDefined && textualBodyFilter.get.processAsText(body.contentType)) {
        copyString(body.asString, resp.getOutputStream, body.contentType.charsetOrUTF8, textualBodyFilter.get.lineProcessor)
      }
      else {
        copyArray(body.asBytes, resp.getOutputStream)
      }

    } finally {
      resp.getOutputStream.close()
    }
  }
}
