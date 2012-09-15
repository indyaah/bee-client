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

package uk.co.bigbeeconsultants.http.header

// See
// - HTTP State Management Mechanism - http://tools.ietf.org/html/rfc6265
// - Internationalized Domain Names - http://tools.ietf.org/html/rfc5890
// - Hypertext Transfer Protocol 1.1 - http://tools.ietf.org/html/rfc2616
// - Hypertext Transfer Protocol 1.1 - http://tools.ietf.org/html/rfc2732
// - Uniform Resource Identifiers - http://tools.ietf.org/html/rfc2396
// - IPv6 addresses - http://tools.ietf.org/html/rfc2732

import java.net.URL
import collection.mutable.ListBuffer

/**
 * CookieJar holds cookies as key/value pairs. It also holds a list of deleted keys to
 * allow the server to mark cookies for deletion; this is used when jars are merged together.
 *
 * Cookie jars are immutable. They are created either programmatically or by processin an HTTP response.
 * In the latter case, once a request has completed, a new instance is created based on the prior cookies and any
 * new ones that were set by the server.
 */
case class CookieJar(cookies: List[Cookie]) {

  def isEmpty = cookies.isEmpty

  /** The number of cookies in this jar. */
  def size = cookies.size

  /**
   * Gets a new `CookieJar` derived from this one as augmented by the headers in a response. This is the primary
   * means for updating a cookie jar after each request.
   * @return a new cookie jar containing the merged cookies.
   */
  private[http] def gleanCookies(url: URL, headers: Headers): CookieJar = {
    val setcookies = filterCookieHeaders(headers)
    if (setcookies.isEmpty) this else CookieParser.updateCookies(this, url, setcookies)
  }

  private def filterCookieHeaders(headers: Headers): List[Header] = {
    headers.list.filter {
      header => header.name == HeaderName.SET_COOKIE.name ||
        header.name == HeaderName.OBSOLETE_SET_COOKIE2.name
    }
  }

  /**
   * Before making a request, use this method to pull the necessary cookies out of the jar.
   * @return an optional cookie header, which will contain one or more cookie values to be sent
   *         with the request.
   */
  private[http] def filterForRequest(url: URL): Option[Header] = {
    val headers = new ListBuffer[String]
    for (cookie <- cookies) {
      if (cookie.willBeSentTo(url)) {
        headers += cookie.asHeader
      }
    }
    if (headers.isEmpty) None else Some(HeaderName.COOKIE -> headers.mkString("; "))
  }

  /**
   * Adds a cookie to this jar or alters the value of an existing cookie.
   * @return a new cookie jar containing the merged cookies.
   */
  def +(cookie: Cookie): CookieJar = {
    val truncated = filterNot(cookie)
    new CookieJar(List(cookie) ++ truncated)
  }

  /**
   * Removes a cookie from this jar, returning a new CookieJar.
   * @return a new cookie jar containing the reduced cookies.
   */
  def -(key: CookieIdentity): CookieJar = {
    new CookieJar(filterNot(key).toList)
  }

  /**
   * Gets a filtered collection of cookies from this jar that match a certain predicate.
   */
  def filter(f: (CookieIdentity) => Boolean): Iterable[Cookie] = {
    cookies.filter(cookie => f(cookie))
  }

  /**
   * Gets a filtered collection of cookies from this jar that match a certain predicate.
   */
  def filter(cookie: CookieIdentity): Iterable[Cookie] = {
    cookies.filter(_ matches cookie)
  }

  /**
   * Gets a filtered collection of cookies from this jar that do not match a certain predicate.
   */
  def filterNot(f: (CookieIdentity) => Boolean): Iterable[Cookie] = {
    cookies.filterNot(cookie => f(cookie))
  }

  /**
   * Gets a filtered collection of cookies from this jar that do not match a certain predicate.
   */
  def filterNot(cookie: CookieIdentity): Iterable[Cookie] = {
    cookies.filterNot(_ matches cookie)
  }

  /**
   * Gets the first cookie from this jar that matches a certain predicate.
   */
  def find(f: (CookieIdentity) => Boolean): Option[Cookie] = {
    cookies.find(cookie => f(cookie))
  }

  /**
   * Gets the first cookie from this jar that has a given name. The domain and path matching terms are ignored
   * in this case.
   */
  def get(name: String): Option[Cookie] = find(_.name == name)

  /**
   * Gets the first cookie from this jar that has a given name/domain/path.
   */
  def get(c: CookieIdentity): Option[Cookie] = find(_ matches c)

  /**
   * Determines whether there is a cookie that matches a certain name/domain/path. This is a shortcut for
   *{{{
   *   find(_ matches c).isDefined
   *}}}
   */
  def contains(c: CookieIdentity): Boolean = find(_ matches c).isDefined
}


/** Provides an easy way to create cookie jars and a constant empty instance. */
object CookieJar {
  /** Constant empty cookie jar. */
  val empty = new CookieJar(List())

  /**
   * Constructs a new cookie jar from an arbitrary collection of cookies. Note that in a normal sequence of HTTP
   * requests, you will not need to use this method. Instead, glean the cookies sent by the server using
   * the two 'gleanCookies' methods.
   */
  def apply(cookies: Cookie*): CookieJar = {
    new CookieJar(List() ++ cookies)
  }
}