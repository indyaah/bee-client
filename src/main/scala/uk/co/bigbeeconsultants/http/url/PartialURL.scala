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

import java.net.{MalformedURLException, URL}
import uk.co.bigbeeconsultants.http.request.SplitURL

/**
 * Provides a utility wrapper for URLs that splits them into their component parts and allows alteration and reassembly
 * with different components. Instances may be relative URLs.
 *
 * Use the case-class 'copy' method to alter components.
 */
case class PartialURL(scheme: Option[String],
                      host: Option[String],
                      port: Option[Int] = None,
                      path: Path = Path.empty,
                      fragment: Option[String] = None,
                      query: Option[String] = None) {

  require(scheme != null && host != null && port != null && path != null && fragment != null && query != null)
  import PartialURL._

  /**
   * Converts this instance to a java.net.URL if possible. This will succeed if isURL would return true.
   * @throws MalformedURLException if some of the necessary information is missing, i.e. this instance is relative.
   */
  lazy val asURL: URL = {
    if (!isURL) throw new MalformedURLException(toString + " cannot be convert to a URL.")

    if (fragment.isEmpty && query.isEmpty)
      new URL(scheme.get, host.get, port.getOrElse(-1), path.toString)
    else
      new URL(toString)
  }

  /** Tests whether this instance is convertible to an absolute URL. */
  def isURL = scheme.isDefined && host.isDefined && (path.isAbsolute || path.isEmpty)

  /**
   * Converts this instance to a [[uk.co.bigbeeconsultants.http.request.SplitURL]] if possible.
   */
  def asSplitURL: SplitURL = {
    if (scheme.isEmpty || host.isEmpty) {
      throw new MalformedURLException(toString + " cannot be convert to a URL.")
    }
    new SplitURL(scheme.get, host.get, port, path.segments, fragment, query)
  }

  /**
   * Gets the host and port parts as a string.
   * E.g. "localhost:8080"
   */
  def hostAndPort: Option[String] = {
    if (host.isEmpty) None
    else Some(host.get + port.map(":" + _.toString).getOrElse(""))
  }

  /** Gets the last path segment, if any. */
  def file: Option[String] = if (path.isEmpty) None else Some(path.segments.last)

  /** Gets the extension of the last path segment, if any. */
  def extension: Option[String] = {
    val f = file
    if (f.isEmpty) None
    else {
      val dot = f.get.lastIndexOf('.')
      if (dot < 0) None else Some(f.get.substring(dot + 1))
    }
  }

  /** Gets the path concatenated with the fragment identifier (if any) and the query parameters (if any). */
  def pathString: String = {
    val frg = fragment.map("#" + _).getOrElse("")
    val q = query.map("?" + _).getOrElse("")
    path.toString + frg + q
  }

  override def toString = {
    val schemeWithSlashes = if (scheme.isEmpty) "" else scheme.get + DoubleSlash
    val hap = hostAndPort
    if (hap.isDefined) schemeWithSlashes + hap.getOrElse("") + pathString else pathString
  }
}


object PartialURL {
  /** The string "://", which is the top of Tim Berners-Lee's regrets. Alas. */
  val DoubleSlash = "://"

  /**
   * Factory method creates an instance from a URL.
   */
  def apply(url: URL): PartialURL = apply(url.toString)

  /**
   * Factory method creates an instance from a string containing a URL.
   */
  def apply(url: String): PartialURL = {
    val hash = url.lastIndexOf('#')
    val query = url.lastIndexOf('?')
    val p1 = url.indexOf(DoubleSlash)
    if (p1 >= 0) parseFullURL(url, p1, hash, query) else parsePartialURL(url, hash, query)
  }

  private def parseFullURL(url: String, p1: Int, hash: Int, query: Int) = {
    val p2 = url.indexOf('/', p1 + 3)
    //val p2 = if (p2a >= 0) p2a else if (url(0) == '/') 0 else url.length
    val colon = url.indexOf(':', p1 + 3)
    val scheme = Some(url.substring(0, p1))
    val host = if (0 < colon) Some(url.substring(p1 + 3, colon)) else if (p2 < 0) Some(url.substring(p1 + 3)) else Some(url.substring(p1 + 3, p2))
    val port = if (0 < colon && colon < p2) Some(url.substring(colon + 1, p2).toInt) else None
    val path = if (p2 < 0) Path.empty else Path(pathStr(url, p2, hash, query))
    new PartialURL(scheme, host, port, path, fragment(url, p2, hash, query), qs(url, p2, hash, query))
  }

  private def parsePartialURL(url: String, hash: Int, query: Int) = {
    val path = Path(pathStr(url, 0, hash, query))
    new PartialURL(None, None, None, path, fragment(url, 0, hash, query), qs(url, 0, hash, query))
  }

  private def pathStr(url: String, slash: Int, hash: Int, query: Int) = {
    if (slash < hash && hash < query) // normal ordering
      url.substring(slash, hash)
    else if (slash < query && query < hash) // non-standard ordering
      url.substring(slash, query)
    else if (slash < hash)
      url.substring(slash, hash)
    else if (slash < query)
      url.substring(slash, query)
    else
      url.substring(slash)
  }

  private def fragment(url: String, slash: Int, hash: Int, query: Int) = {
    if (slash < hash && hash < query)
      Some(url.substring(hash + 1, query))
    else if (slash < hash)
      Some(url.substring(hash + 1))
    else
      None
  }

  private def qs(url: String, slash: Int, hash: Int, query: Int) = {
    if (slash < query && query < hash) // non-standard ordering
      Some(url.substring(query + 1, hash))
    else if (slash < query)
      Some(url.substring(query + 1))
    else
      None
  }

  private def portAsOption(port: Int) = if (port < 0) None else Some(port)

  private def strAsOption(str: String) = if (str != null && str.length > 0) Some(str) else None
}
