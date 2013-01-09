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

import java.net.{URLEncoder, MalformedURLException, URL}
import uk.co.bigbeeconsultants.http.HttpClient

/**
 * Provides a utility wrapper for URLs that splits them into their component parts and allows alteration and reassembly
 * with different components. Instances may be relative URLs.
 *
 * Use the case-class 'copy' method to alter components.
 */
case class Href(endpoint: Option[Endpoint],
                path: Path = Path.empty,
                fragment: Option[String] = None,
                query: Option[String] = None) {

  require(endpoint != null && path != null && fragment != null && query != null)

  /**
   * Converts this instance to a java.net.URL if possible. This will succeed if isURL would return true.
   * @throws MalformedURLException if some of the necessary information is missing, i.e. this instance is relative.
   */
  lazy val asURL: URL = {
    if (!isURL) throw new MalformedURLException(toString + " cannot be convert to a URL.")

    if (fragment.isEmpty && query.isEmpty) {
      val ep = endpoint.get
      new URL(ep.scheme, ep.host, ep.port.getOrElse(-1), path.toString)
    } else {
      new URL(toString)
    }
  }

  /** Tests whether this instance is convertible to an absolute URL. */
  def isURL = endpoint.isDefined && (path.isAbsolute || path.isEmpty)

  /**
   * Tests whether this instances starts with another instance. This is true if both endpoints are the same
   * (possibly None) and this path starts with the other path.
   */
  def startsWith(other: Href): Boolean = {
    this.endpoint == other.endpoint && this.path.startsWith(other.path)
  }

  /**
   * Gets the host and port parts as a string.
   * E.g. "localhost:8080"
   */
  def hostAndPort: Option[String] = {
    endpoint.map(_.hostAndPort)
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
    endpoint.map(_.toString).getOrElse("") + pathString
  }

  /** Creates a new instance, replacing any query string with a new one formed from a map of key/values pairs. */
  def withQuery(params: Map[String, String]) = {
    def assembleQueryString(queryParams: Map[String, String]): String = {
      val w = new StringBuilder
      var amp = ""
      for ((key, value) <- queryParams) {
        w.append(amp)
        w.append(URLEncoder.encode(key, HttpClient.UTF8))
        w.append('=')
        w.append(URLEncoder.encode(value, HttpClient.UTF8))
        amp = "&"
      }
      w.toString()
    }

    val newValue = if (params.isEmpty) None else Some(assembleQueryString(params))
    copy(query = newValue)
  }

  def toPartialURL = new PartialURL(endpoint, path, fragment, query)
}

//---------------------------------------------------------------------------------------------------------------------

object Href {
  /**
   * Factory method creates an instance from a URL.
   */
  def apply(url: URL): Href = apply(url.toString)

  /**
   * Factory method creates an instance from a string containing a URL.
   */
  def apply(url: String): Href = {
    val hash = url.lastIndexOf('#')
    val query = url.lastIndexOf('?')
    val p1 = url.indexOf(Endpoint.DoubleSlash)
    if (p1 >= 0) parseFullURL(url, p1, hash, query) else parsePartialURL(url, hash, query)
  }

  private val root = new Path(true, Nil)

  private def parseFullURL(url: String, p1: Int, hash: Int, query: Int) = {
    val p2 = url.indexOf('/', p1 + 3)
    val s3 = if (p2 < 0) url.length else p2
    val path = if (p2 < 0) root else Path(pathStr(url, p2, hash, query))
    val endpoint = Endpoint(url.substring(0, s3))
    new Href(Some(endpoint), path, fragment(url, p2, hash, query), qs(url, p2, hash, query))
  }

  private def parsePartialURL(url: String, hash: Int, query: Int) = {
    val path = Path(pathStr(url, 0, hash, query))
    new Href(None, path, fragment(url, 0, hash, query), qs(url, 0, hash, query))
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

  //  private def portAsOption(port: Int) = if (port < 0) None else Some(port)
  //
  //  private def strAsOption(str: String) = if (str != null && str.length > 0) Some(str) else None
}
