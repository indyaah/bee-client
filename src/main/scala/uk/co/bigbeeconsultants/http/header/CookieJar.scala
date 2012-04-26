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
import uk.co.bigbeeconsultants.http.response.Response
import collection.mutable.{HashSet, LinkedHashMap, ListBuffer}
import collection.immutable.ListMap

/**
 * CookieJar holds cookies as key/value pairs. It also holds a list of deleted keys to
 * allow the server to mark cookies for deletion; this is used when jars are merged together.
 */
case class CookieJar(cookies: ListMap[CookieKey, CookieValue] = ListMap (), deleted: Set[CookieKey] = Set ()) {

  /**
   * Allows cookie jars to be merged together. As newJar is merged into this cookie jar, it trumps
   * any matching cookies already in this jar.
   * @return a new cookie jar containing the merged cookies.
   */
  def merge(newJar: CookieJar): CookieJar = {
    val jar = new LinkedHashMap[CookieKey, CookieValue]
    jar ++= cookies

    val del = new HashSet[CookieKey]
    del ++= deleted

    for ((key, value) <- newJar.cookies) {
      jar.put (key, value)
      del.remove (key)
    }

    for (key <- newJar.deleted) {
      jar.remove (key)
      del.add (key)
    }

    new CookieJar (ListMap() ++ jar, del.toSet)
  }

  /**
   * Gets a new CookieJar derived from this one as augmented by the headers in a response.
   * @return a new cookie jar containing the merged cookies.
   */
  def updateCookies(response: Response): CookieJar = {
    val setcookies = filterCookieHeaders (response)
    if (setcookies.isEmpty) {
      this
    }
    else {
      CookieParser.updateCookies (this, response.request.url, setcookies)
    }
  }

  private def filterCookieHeaders(response: Response): List[Header] = {
    response.headers.list.filter (
      header => header.name == HeaderName.SET_COOKIE.name ||
        header.name == HeaderName.OBSOLETE_SET_COOKIE2.name)
  }

  /**
   * Before making a request, use this method to pull the necessary cookies out of the jar.
   * @return an optional cookie header, which will contain one or more cookie values to be sent
   * with the request.
   */
  def filterForRequest(url: URL): Option[Header] = {
    val headers = new ListBuffer[String]
    for ((key, value) <- cookies) {
      val cookie = Cookie (key, value)
      if (cookie.willBeSentTo (url)) {
        headers += cookie.asHeader
      }
    }
    if (headers.isEmpty) None else Some (HeaderName.COOKIE -> headers.mkString ("; "))
  }

  /**
   * Adds a cookie to this jar, returning a new CookieJar.
   */
  def + (key: CookieKey, value: CookieValue): CookieJar = {
    new CookieJar(cookies + (key -> value), deleted - key)
  }

  /**
   * Adds a cookie to this jar, returning a new CookieJar.
   */
  def + (cookie: Cookie): CookieJar = {
    new CookieJar(cookies + (cookie.key -> cookie.value), deleted - cookie.key)
  }

  /**
   * Removes a cookie from this jar, returning a new CookieJar.
   */
  def - (key: CookieKey): CookieJar = {
    new CookieJar(cookies - key, deleted - key)
  }
}


object CookieJar {
  /**Constant empty cookie jar. */
  val empty = new CookieJar ()

  /**
   * Constructs a new cookie jar containing all the cookies (if any) that are received in the response.
   */
  def harvestCookies(response: Response): CookieJar = new CookieJar ().updateCookies (response)
}