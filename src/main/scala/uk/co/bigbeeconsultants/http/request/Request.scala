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

package uk.co.bigbeeconsultants.http.request

import java.net.URL
import uk.co.bigbeeconsultants.http.header.{CookieJar, Header, Headers}
import uk.co.bigbeeconsultants.http.url.Href

//TODO: need to support chunked streaming out of the request
/**
 * Represents the requirements for an HTTP request. Immutable.
 * Normally, this class will not be instantiated directly but via the Request object methods.
 */
final case class Request(method: String,
                         url: URL,
                         body: Option[RequestBody] = None,
                         headers: Headers = Headers.empty,
                         cookies: Option[CookieJar] = None) {
  require(method == method.toUpperCase, method + " must be uppercase.")
  require(url != null, "URL cannot be null.")
  require(headers != null, "Headers cannot be null.")

  /** Gets the URL split into its component parts. */
  lazy val href = Href(url)
  @deprecated
  lazy val split = Href(url)

  /** Gets the request without any headers. */
  def withoutHeaders = this.copy(headers = Headers.Empty)

  /** Gets the request without any cookies. */
  def withoutCookies = this.copy(cookies = None)

  /** Adds another header to the collection of request headers. */
  def +(header: Header) = this.copy(headers = this.headers + header)

  /** Adds more headers to the collection of request headers. */
  def +(moreHeaders: Headers) = this.copy(headers = this.headers ++ moreHeaders)

  /** Provides the cookie jar to be used with this request. This replaces any previous setting. */
  def +(cookies: CookieJar): Request = using(Some(cookies))

  /** Provides the cookie jar to be used with this request. This replaces any previous setting. */
  def +(cookies: Option[CookieJar]): Request = using(cookies)

  /** Provides the cookie jar to be used with this request. This replaces any previous setting. An alias for '+'. */
  def using(cookies: CookieJar): Request = using(Some(cookies))

  /** Provides the cookie jar to be used with this request. This replaces any previous setting. An alias for '+'. */
  def using(cookies: Option[CookieJar]): Request = this.copy(cookies = cookies)

  def isDelete = method == Request.DELETE

  def isGet = method == Request.GET

  def isHead = method == Request.HEAD

  def isOptions = method == Request.OPTIONS

  def isPost = method == Request.POST

  def isPut = method == Request.PUT

  def isTrace = method == Request.TRACE

  /**
   * Creates a new instance, setting the query string to be a new one formed from a map of key/values pairs.
   * If the map is empty, any previous query parameters are removed.
   */
  def withQuery(params: Map[String, String]) = {
    copy(url = href.withQuery(params).asURL)
  }

  def toShortString = {
    val bodyStr = if (body.isEmpty) "-" else body.get.toShortString
    method + ' ' + url + ' ' + bodyStr + ' ' + headers.list.mkString("[", ";", "]")
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides factory methods for creating request objects of various types. These include
 * <ul>
 * <li>methods without an entity body: get, head, delete, trace</li>
 * <li>method with an optional entity body: options</li>
 * <li>methods requiring an entity body: post, put</li>
 * </ul>
 */
object Request {
  val DELETE = "DELETE"
  val GET = "GET"
  val HEAD = "HEAD"
  val OPTIONS = "OPTIONS"
  val POST = "POST"
  val PUT = "PUT"
  val TRACE = "TRACE"

  // methods without an entity body
  def get(url: URL, headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(GET, url, None, headers, cookies)

  def head(url: URL, headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(HEAD, url, None, headers, cookies)

  def delete(url: URL, headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(DELETE, url, None, headers, cookies)

  def trace(url: URL, headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(TRACE, url, None, headers, cookies)

  // method with an optional entity body
  def options(url: URL, body: Option[RequestBody] = None, headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(OPTIONS, url, body, headers, cookies)

  def post(url: URL, body: Option[RequestBody], headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(POST, url, body, headers, cookies)

  // methods requiring an entity body
  def put(url: URL, body: RequestBody, headers: Headers = Headers.Empty, cookies: Option[CookieJar] = None): Request =
    Request(PUT, url, Some(body), headers, cookies)

  val Empty = get("http://localhost/")
}