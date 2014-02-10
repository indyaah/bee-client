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

import header.{CookieJar, Headers}
import java.io.IOException
import java.net.URL
import request.{RequestBody, Request}
import response.{UnbufferedResponseBuilder, BufferedResponseBuilder, ResponseBuilder, Response}

trait HttpExecutor {
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
  def execute(request: Request, responseBuilder: ResponseBuilder, config: Config)
}

/**
 * Describes an API for making HTTP requests.
 */
abstract class Http(val commonConfig: Config = Config()) extends HttpExecutor {

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
   * Makes an arbitrary request and returns the response, which contains the entire response body in a buffer. All the
   * convenience methods (head, get, put, post, trace, options) use this buffered method.
   *
   * It is the 'normal' way to make HTTP requests. Bit it may be inappropriate when the response body is too large to
   * be buffered in memory, or if it is desirable to start processing the body whilst it is still being read in, in
   * which case use 'makeUnbufferedRequest' instead.
   *
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
   * Makes an arbitrary request and returns a the response. For successful 200-OK responses, this will be an
   * *unbuffered* response with direct access to the source input stream. This allows bespoke usage of this
   * stream. It is the caller's responsibility to ensure that the stream is always closed, so preventing
   * leakage of resources. This is made easier because the stream is of a kind that will
   * automatically close as soon as all the data has been read.
   *
   * For all responses other than 200-OK, a buffered response is returned, as if `makeRequest` had been used instead.
   * This ensures that the input stream is automatically closed for all error, redirection and no-content responses.
   * @param request the request
   * @param config the particular configuration being used for this request; defaults to the commonConfiguration
   *               supplied to this instance of HttpClient
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @return the response (for all outcomes including 4xx and 5xx status codes) if
   *         no exception occurred
   */
  @throws(classOf[IOException])
  def makeUnbufferedRequest(request: Request, config: Config = commonConfig): Response = {
    val responseBuilder = new UnbufferedResponseBuilder
    execute(request, responseBuilder, config)
    responseBuilder.response.get
  }
}
