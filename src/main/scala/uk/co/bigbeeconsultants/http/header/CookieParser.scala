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
import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.HttpDateTimeInstant
import uk.co.bigbeeconsultants.http.url.Domain

private[header] object CookieParser {
  val DOMAIN = "Domain"
  val SECURE = "Secure"
  val HTTPONLY = "HttpOnly"
  val MAX_AGE = "Max-Age"
  val EXPIRES = "Expires"
  val PATH = "Path"

  private[header] def parseOneCookie(line: String, from: URL, requestPath: String, now: HttpDateTimeInstant): Option[Cookie] = {
    var name: String = ""
    var value: String = ""
    var path: String = requestPath
    var expires: Option[HttpDateTimeInstant] = None
    var domain: Domain = Domain(from.getHost)
    var hostOnly = true
    var secure = false
    var httpOnly = false
    var maxAge: Option[Int] = None

    for (attr <- HttpUtil.split(line, ';')) {

      val t = HttpUtil.divide(attr, '=')
      val a = t._1.trim
      val v = t._2.trim

      if (name == "") {
        name = a
        value = v
      }
      else if (a.equalsIgnoreCase(DOMAIN)) {
        // TODO reject cookies in the public suffix list http://publicsuffix.org/list/
        domain = Domain(v)
        hostOnly = false
      }
      else if (a.equalsIgnoreCase(SECURE) && v == "") {
        secure = true
      }
      else if (a.equalsIgnoreCase(HTTPONLY) && v == "") {
        httpOnly = true
        if (!from.getProtocol.startsWith("http"))
          return None
      }
      else if (a.equalsIgnoreCase(MAX_AGE)) {
        maxAge = Some(v.toInt)
      }
      else if (a.equalsIgnoreCase(EXPIRES)) {
        expires = Some(HttpDateTimeInstant.parse(v))
      }
      else if (a.equalsIgnoreCase(PATH)) {
        path = v
      }
    }

    if (!path.endsWith("/")) {
      path += "/"
    }

    val persistent = maxAge.isDefined || expires.isDefined

    Some(Cookie(name, value, domain, path,
      maxAge, expires, now, persistent, hostOnly, secure, httpOnly, from.getProtocol))
  }


  /** Gets a new CookieJar derived from this one as augmented by the headers in a response. */
  def updateCookies(previous: CookieJar, from: URL, setcookies: List[Header]): CookieJar = {

    var newJar = previous

    val fPath = from.getPath
    val lastSlash = fPath.lastIndexOf('/')
    val path = if (lastSlash < 0) "/" else fPath.substring(0, lastSlash + 1)

    // Construct the date only once - avoids rollover problems (which would be a bit like race conditions)
    val now = new HttpDateTimeInstant()

    for (header <- setcookies) {
      for (line <- HttpUtil.split(header.value, '\n')) {
        val optCookie = parseOneCookie(line, from, path, now)
        if (optCookie.isDefined) {
          val cookie = optCookie.get
          if (cookie.expires.isDefined && cookie.expires.get < now) {
            newJar -= cookie
          } else {
            newJar += cookie
          }
        }
      }
    }

    newJar
  }

  /**
   * Converts a cookie to its 'Set-Cookie' string.
   */
  def asSetHeader(cookie: Cookie) = {
    val exp = cookie.expires.map("; " + EXPIRES + '=' + _).getOrElse("")
    cookie.name + '=' + cookie.value + "; " +
      PATH + '=' + cookie.path + "; " +
      DOMAIN + '=' + cookie.domain.domain +
      exp
  }
}