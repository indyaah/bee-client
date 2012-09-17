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

import header._
import java.net.URL
import java.io.IOException
import request.{RequestBody, Request}
import response._

/**
 * Provides an alternative to [[uk.co.bigbeeconsultants.http.HttpClient]]
 * in which every request carries an outbound cookie jar and every
 * inbound response potentially provides a modified cookie jar. A series of requests will therefore behave like
 * a normal web-browser by aggregating cookies from the server and returning them back in subsequent requests.
 *
 * Concurrent requests with an instance of this class are not forbidden. But if such concurrent requests *are* made,
 * there will be race conditions that may or may not cause some updates to be overwritten, depending on the server
 * behaviour. the cookie jars themselves are immutable so will be in either of two states: the original state or
 * the updated state.
 */
class HttpBrowser(val config: Config = Config(),
                  val commonRequestHeaders: Headers = HttpClient.defaultRequestHeaders,
                  initialCookieJar: CookieJar = CookieJar.empty) {

  private val httpClient = new HttpClient(config, commonRequestHeaders)
  private var _cookieJar: Option[CookieJar] = Some(initialCookieJar)

  /** Gets the current state of the cookie jar. */
  def cookies = _cookieJar.get

  /**
   * Make a HEAD request.
   */
  @throws(classOf[IOException])
  def head(url: URL, requestHeaders: Headers = Nil): Response = {
    execute(Request.head(url) + requestHeaders)
  }

  /**
   * Make a TRACE request.
   */
  @throws(classOf[IOException])
  def trace(url: URL, requestHeaders: Headers = Nil): Response = {
    execute(Request.trace(url) + requestHeaders)
  }

  /**
   * Make a GET request.
   */
  @throws(classOf[IOException])
  def get(url: URL, requestHeaders: Headers = Nil): Response = {
    execute(Request.get(url) + requestHeaders)
  }

  /**
   * Make a DELETE request.
   */
  @throws(classOf[IOException])
  def delete(url: URL, requestHeaders: Headers = Nil): Response = {
    execute(Request.delete(url) + requestHeaders)
  }

  /**
   * Make an OPTIONS request.
   */
  @throws(classOf[IOException])
  def options(url: URL, body: Option[RequestBody], requestHeaders: Headers = Nil): Response = {
    execute(Request.options(url, body) + requestHeaders)
  }

  /**
   * Make a POST request.
   */
  @throws(classOf[IOException])
  def post(url: URL, body: Option[RequestBody], requestHeaders: Headers = Nil): Response = {
    execute(Request.post(url, body) + requestHeaders)
  }

  /**
   * Make a PUT request.
   */
  @throws(classOf[IOException])
  def put(url: URL, body: RequestBody, requestHeaders: Headers = Nil): Response = {
    execute(Request.put(url, body) + requestHeaders)
  }

  /**
   * Makes an arbitrary request and returns the response. The entire response body is read into memory.
   * @param request the request
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @return the response (for all outcomes including 4xx and 5xx status codes) if
   *         no exception occurred
   */
  @throws(classOf[IOException])
  def execute(request: Request): Response = {
    val response = httpClient.execute(request using _cookieJar)
    // here's a potential race condition which we happily ignore
    _cookieJar = response.cookies
    response
  }
}
