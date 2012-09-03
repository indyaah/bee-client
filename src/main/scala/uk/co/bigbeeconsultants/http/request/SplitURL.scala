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

import java.net.{URLEncoder, URL}
import uk.co.bigbeeconsultants.http.HttpClient

/**
 * Provides a utility wrapper for URLs that splits them into their component parts and allows alteration and reassembly
 * with different components. Use the case-class 'copy' method to alter components.
 */
case class SplitURL(scheme: String,
                    host: String,
                    port: Option[Int] = None,
                    pathSegments: List[String] = Nil,
                    fragment: Option[String] = None,
                    query: Option[String] = None) {

  def asURL: URL =
    if (fragment.isEmpty && query.isEmpty)
      new URL(scheme, host, port.getOrElse(-1), path)
    else
      new URL(toString)

  def hostAndPort = host + port.map(":" + _.toString).getOrElse("")

  def path = pathSegments.mkString("/", "/", "")

  def pathString = path + fragment.map("#" + _).getOrElse("") + query.map("?" + _).getOrElse("")

  override def toString = scheme + "://" + hostAndPort + pathString

  def withQuery(params: Map[String, String]) = {
    copy(query = Some(SplitURL.assembleQueryString(params)))
  }
}


object SplitURL {
  /**
   * Factory method creates an instance from a URL.
   */
  def apply(url: URL) = new SplitURL(url.getProtocol, url.getHost, portAsOption(url.getPort),
    trimAndSplitPath(url.getFile), strAsOption(url.getRef), strAsOption(url.getQuery))

  /**
   * Factory method creates an instance from a string containing a URL.
   */
  def apply(url: String) = {
    val p1 = url.indexOf("://")
    require(p1 > 0, "Malformed URL: no scheme part.")
    val p2 = url.indexOf('/', p1 + 3)
    require(p2 > p1, "Malformed URL: no host part")
    // optional parts
    val colon = url.indexOf(':', p1 + 3)
    val hash = url.lastIndexOf('#')
    val query = url.lastIndexOf('?')

    val scheme = url.substring(0, p1)
    val host = if (colon > 0) url.substring(p1 + 3, colon) else url.substring(p1 + 3, p2)
    val port = if (colon > 0) Some(url.substring(colon + 1, p2).toInt) else None

    val path =
      if (p2 < hash && hash < query)
        url.substring(p2 + 1, hash)
      else if (0 < query && query < hash) // non-standard ordering
        url.substring(p2 + 1, query)
      else
        url.substring(p2 + 1)

    val fragment =
      if (p2 < hash && hash < query)
        Some(url.substring(hash + 1, query))
      else if (p2 < hash)
        Some(url.substring(hash + 1))
      else
        None

    val qs =
      if (p2 < query && query < hash) // non-standard ordering
        Some(url.substring(query + 1, hash))
      else if (p2 < query)
        Some(url.substring(query + 1))
      else
        None

    new SplitURL(scheme, host, port, splitPathWithoutLeadingSlash(path), fragment, qs)
  }

  /**
   * Factory method creates an instance from several fields.
   * The fragment and query parameters are nullable. The port parameter can be negative to indicate default.
   */
  def apply(scheme: String, host: String, port: Int, path: String, fragment: String, query: String) = {
    new SplitURL(scheme, host, portAsOption(port), trimAndSplitPath(path), Option(fragment), Option(query))
  }

  private def portAsOption(port: Int) = if (port < 0) None else Some(port)

  private def strAsOption(str: String) = if (str != null && str.length > 0) Some(str) else None

  private def trimAndSplitPath(path: String): List[String] = {
    if (path.length == 0 || path(0) != '/') {
      val q = path.indexOf('?')
      if (q > 0)
        splitPathWithoutLeadingSlash(path.substring(0, q))
      else
        splitPathWithoutLeadingSlash(path)
    }
    else {
      trimAndSplitPath(path.substring(1))
    }
  }

  private def splitPathWithoutLeadingSlash(path: String) = if (path.length > 0) List() ++ path.split('/') else Nil

  implicit def convertToURL(splitUrl: SplitURL): URL = splitUrl.asURL

  implicit def convertFromURL(url: URL): SplitURL = apply(url)

  implicit def convertFromString(url: String): SplitURL = apply(url)

  private def assembleQueryString(queryParams: Map[String, String]) = {
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
}
