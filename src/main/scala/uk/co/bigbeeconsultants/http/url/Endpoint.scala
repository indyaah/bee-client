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

package uk.co.bigbeeconsultants.http.url

/**
 * Defines a service endpoint, which is typically a webserver accessed via HTTP. This is equivalent to a URL
 * without any path, fragment or query string.
 * @param scheme the scheme - typically "http" or "https"
 * @param host the hostname or IP address
 * @param port the optional TCP port number
 */
case class Endpoint(scheme: String, userinfo: Option[String], host: String, port: Option[Int]) {
  require(scheme != null && userinfo != null && host != null && port != null)

  def this(scheme: String, host: String, port: Option[Int] = None) = this(scheme, None, host, port)

  /** Gets the corresponding domain. */
  lazy val domain = Domain(host)

  /**
   * Gets the host and port parts as a string. This doesn't include the userinfo.
   * E.g. "localhost:8080"
   */
  def hostAndPort: String = host + port.map(":" + _.toString).getOrElse("")

  /**
   * Gets the userinfo, host and port parts as a string. Note that either userinfo and/or port may be absent.
   * E.g. "john@myserver.com:8080" or just "myserver.com".
   */
  def authority: String = userinfoString + hostAndPort

  private def userinfoString = if (userinfo.isDefined) userinfo.get + "@" else ""

  override def toString = scheme + Endpoint.DoubleSlash + userinfoString + hostAndPort
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines a service endpoint, which is typically a webserver accessed via HTTP. This is equivalent to a URL
 * without any path, fragment or query string.
 */
object Endpoint {
  /** The string "://", which is the top of Tim Berners-Lee's regrets. Alas. */
  val DoubleSlash = "://"

  /** Constructs an instance from parameters, provided they are all defined. */
  def apply(scheme: Option[String], host: Option[String], port: Option[Int]): Option[Endpoint] = {
    apply(scheme, None, host, port)
  }

  /** Constructs an instance from parameters, provided they are all defined. */
  def apply(scheme: Option[String], userinfo: Option[String], host: Option[String], port: Option[Int]): Option[Endpoint] = {
    require(scheme.isDefined == host.isDefined, "The scheme and host must both be defined, or neither must be defined.")
    if (scheme.isDefined) Some(new Endpoint(scheme.get, userinfo, host.get, port)) else None
  }

  /** Constructs an instance from parameters. */
  def apply(scheme: String, host: Domain, port: Option[Int]): Endpoint = {
    new Endpoint(scheme, host.domain, port)
  }

  /** Constructs an instance from the first part of a URL string. The remainder is ignored. */
  def apply(url: String): Endpoint = {
    val p1 = url.indexOf(DoubleSlash)
    require(p1 > 0, url + " must start with the scheme and double-slash.")
    val at = url.indexOf('@', p1 + 3)
    val from = if (at > 0) {
      val colon = url.indexOf(':', p1 + 3)
      require(colon < 0 || colon > at, url + " must not include any password (deprecated by RFC3986).")
      at + 1
    } else p1 + 3
    val p2 = url.indexOf('/', from)
    val s3 = if (p2 < 0) url.length else p2
    val colon = url.indexOf(':', from)
    val scheme = url.substring(0, p1)
    val userinfo = if (at > 0) Some(url.substring(p1 + 3, at)) else None
    val host = if (0 < colon) url.substring(from, colon) else url.substring(from, s3)
    val port = if (0 < colon && colon < s3) Some(url.substring(colon + 1, s3).toInt) else None
    new Endpoint(scheme, userinfo, host, port)
  }
}
