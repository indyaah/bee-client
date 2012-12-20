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
case class Endpoint(scheme: String, host: String, port: Option[Int] = None) {
  require(scheme != null && host != null && port != null)

  /** Gets the corresponding domain. */
  lazy val domain = Domain(host)

  /**
   * Gets the host and port parts as a string.
   * E.g. "localhost:8080"
   */
  def hostAndPort: String = host + port.map(":" + _.toString).getOrElse("")

  override def toString = scheme + Endpoint.DoubleSlash + hostAndPort
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
    require(scheme.isDefined == host.isDefined, "The scheme and host must both be defined, or neither must be defined.")
    if (scheme.isDefined) Some(new Endpoint(scheme.get, host.get, port)) else None
  }

  /** Constructs an instance from parameters. */
  def apply(scheme: String, host: Domain, port: Option[Int]): Endpoint = {
    new Endpoint(scheme, host.domain, port)
  }

  /** Constructs an instance from the first part of a URL string. The remainder is ignored. */
  def apply(url: String): Endpoint = {
    val p1 = url.indexOf(DoubleSlash)
    require(p1 > 0, url + " must start with the scheme and double-slash.")
    val p2 = url.indexOf('/', p1 + 3)
    val s3 = if (p2 < 0) url.length else p2
    val colon = url.indexOf(':', p1 + 3)
    val scheme = url.substring(0, p1)
    val host = if (0 < colon) url.substring(p1 + 3, colon) else url.substring(p1 + 3, s3)
    val port = if (0 < colon && colon < s3) Some(url.substring(colon + 1, s3).toInt) else None
    Endpoint(scheme, host, port)
  }
}
