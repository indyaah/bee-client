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

package uk.co.bigbeeconsultants.lhc.header

// See
// - HTTP State Management Mechanism - http://tools.ietf.org/html/rfc6265
// - Internationalized Domain Names - http://tools.ietf.org/html/rfc5890
// - Hypertext Transfer Protocol 1.1 - http://tools.ietf.org/html/rfc2616
// - Hypertext Transfer Protocol 1.1 - http://tools.ietf.org/html/rfc2732
// - Uniform Resource Identifiers - http://tools.ietf.org/html/rfc2396
// - IPv6 addresses - http://tools.ietf.org/html/rfc2732

import java.net.URL
import uk.co.bigbeeconsultants.lhc.response.Response
import collection.mutable.{ListBuffer, LinkedHashMap}
import uk.co.bigbeeconsultants.lhc.{HttpDateTimeInstant, Util}

case class CookieJar(cookies: Map[CookieKey, CookieValue] = Map()) {

  private def parseOneCookie(line: String, scheme: String, host: String, requestPath: String, now: HttpDateTimeInstant): Option[Cookie] = {
    var name: String = ""
    var value: String = ""
    var path: String = requestPath
    var expires = now
    var domain: Domain = Domain(host)
    var hostOnly = true
    var persistent = false
    var secure = false
    var httpOnly = false
    var hasMaxAge = false

    for (attr <- Util.split(line, ';')) {

      val t = Util.divide(attr, '=')
      val a = t._1.trim
      val v = t._2.trim

      if (name == "") {
        name = a
        value = v
      }
      else if (a.equalsIgnoreCase("Domain")) {
        // TODO reject cookies in the public suffix list http://publicsuffix.org/list/
        domain = Domain(v)
        hostOnly = false
      }
      else if (a.equalsIgnoreCase("Secure") && v == "") {
        secure = true
      }
      else if (a.equalsIgnoreCase("HttpOnly") && v == "") {
        httpOnly = true
        if (!scheme.startsWith("http"))
          return None
      }
      else if (a.equalsIgnoreCase("Max-Age")) {
        persistent = true
        hasMaxAge = true
        val seconds = v.toLong
        val secDelta = if (seconds > 0) seconds else 0
        expires = now + secDelta
      }
      else if (a.equalsIgnoreCase("Expires") && !hasMaxAge) {
        persistent = true
        expires = HttpDateTimeInstant.parse(v)
      }
      else if (a.equalsIgnoreCase("Path")) {
        path = v
      }
    }

    if (!path.endsWith("/")) {
      path += "/"
    }

    val k = CookieKey(name, domain, path)
    val v = CookieValue(value, expires, now, now, persistent, hostOnly, secure, httpOnly, scheme)
    Some(Cookie(k, v))
  }

  private def filterCookieHeaders(response: Response): List[Header] = {
    response.headers.list.filter(
      header => header.name == HeaderName.SET_COOKIE.name ||
        header.name == HeaderName.OBSOLETE_SET_COOKIE2.name)
  }

  /**Gets a new CookieJar derived from this one as augmented by the headers in a response. */
  def updateCookies(response: Response): CookieJar = {
    val setcookies = filterCookieHeaders(response)
    if (setcookies.isEmpty) {
      this
    }
    else {
      val from = response.request.url
      val host = Util.divide(from.getAuthority, ':')._1
      val fPath = from.getPath
      val fullPath = if (fPath == "") "/" else Util.divide(fPath, '?')._1
      val lastSlash = fullPath.lastIndexOf('/')
      val path = if (fullPath == "/") "/" else fullPath.substring(0, lastSlash)
      // Construct the date only once - avoids rollover problems (which would be a bit like race conditions)
      val now = new HttpDateTimeInstant()

      val jar = new LinkedHashMap[CookieKey, CookieValue]
      jar ++= cookies

      for (header <- setcookies) {
        for (line <- Util.split(header.value, '\n')) {
          val optCookie = parseOneCookie(line, from.getProtocol, host, path, now)
          if (optCookie.isDefined) {
            val cookie = optCookie.get
            if (cookie.value.expires < now) {
              jar.remove(cookie.key)
            } else {
              jar.put(cookie.key, cookie.value)
            }
          }
        }
      }

      CookieJar(jar.toMap)
    }
  }


  def filterForRequest(url: URL): Header = {
    val headers = new ListBuffer[String]
    for ((key, value) <- cookies) {
      val cookie = Cookie(key, value)
      if (cookie.willBeSentTo(url)) {
        headers += cookie.asHeader
      }
    }
    HeaderName.COOKIE -> headers.mkString("; ")
  }
}


object CookieJar {
  def updateCookies(response: Response): CookieJar = new CookieJar().updateCookies(response)
}