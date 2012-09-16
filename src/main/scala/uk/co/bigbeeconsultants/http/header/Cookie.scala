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
import uk.co.bigbeeconsultants.http.HttpDateTimeInstant

/**
 * Defines the elements necessary to distinguish one cookie from another.
 */
trait CookieIdentity {
  def name: String

  def domain: Domain

  def path: String

  def matches(cookie: CookieIdentity) = {
    name == cookie.name &&
      domain == cookie.domain &&
      path == cookie.path
  }
}

/**
 * Provides an implementation of [[uk.co.bigbeeconsultants.http.header.CookieIdentity]] without any
 * particular value information.
 */
case class CookieKey(name: String,
                     domain: Domain = Domain.localhost,
                     path: String = "/") extends CookieIdentity {
  require(name.length > 0)
  require(path.endsWith("/"), path)

  /**
   * Constructs a [[uk.co.bigbeeconsultants.http.header.Cookie]] by providing a value for this key.
   */
  def ->(value: String,
         expires: Option[HttpDateTimeInstant] = None,
         creation: HttpDateTimeInstant = new HttpDateTimeInstant(),
         persistent: Boolean = false,
         hostOnly: Boolean = false,
         secure: Boolean = false,
         httpOnly: Boolean = false,
         serverProtocol: String = "http") =
    new Cookie(name, value, domain, path, expires, creation, persistent, hostOnly,
      secure, httpOnly, serverProtocol)
}


/**
 * Defines a complete cookie in terms of its identity and its value.
 */
case class Cookie(name: String,
                  value: String,
                  domain: Domain = Domain.localhost,
                  path: String = "/",
                  expires: Option[HttpDateTimeInstant] = None,
                  creation: HttpDateTimeInstant = new HttpDateTimeInstant(),
                  persistent: Boolean = false,
                  hostOnly: Boolean = false,
                  secure: Boolean = false,
                  httpOnly: Boolean = false,
                  serverProtocol: String = "http") extends CookieIdentity {
  require(name.length > 0)
  require(path.endsWith("/"), path)

  /** Gets the cookie as a request header value. */
  def asHeader = name + "=" + value

  /** Tests whether this cookie will be sent in the headers of a request to a specified URL. */
  def willBeSentTo(url: URL) = {
    val p = url.getProtocol
    val qSecure = !secure || p == "https"
    val qHttpOnly = !httpOnly || p.startsWith("http")
    val qDomain = domain matches url
    val qPath = path.isEmpty || url.getPath.startsWith(path)
    qSecure && qHttpOnly && qDomain && qPath
  }
}
