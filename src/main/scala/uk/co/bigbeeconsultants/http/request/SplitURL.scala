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

import java.net.URL

/**
 * Provides a utility wrapper for URLs that splits them into their component parts and allows alteration and reassembly
 * with different components. Use the case-class 'copy' method to alter components.
 */
case class SplitURL(scheme: String,
                    host: String,
                    port: Option[Int],
                    path: List[String],
                    fragment: Option[String],
                    query: Option[String]) {

  def asURL: URL =
    if (fragment.isEmpty && query.isEmpty)
      new URL(scheme, host, port.getOrElse(-1), "/" + path.mkString("/"))
    else
      new URL(toString)

  override def toString = scheme + "://" + host + port.map(":" + _.toString).getOrElse("") +
    "/" + path.mkString("/") + fragment.map("#" + _).getOrElse("") + query.map("?" + _).getOrElse("")
}


object SplitURL {
  /** Factory method creates an instance from a URL. */
  def apply(url: URL) = new SplitURL(url.getProtocol, url.getHost, portAsOption(url.getPort),
    trimAndSplitPath(url.getFile), strAsOption(url.getRef), strAsOption(url.getQuery))

  /** Factory method creates an instance from a string containing a URL. */
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

  private def portAsOption(port: Int) = if (port > 0) Some(port) else None

  private def strAsOption(str: String) = if (str != null && str.length > 0) Some(str) else None

  private def trimAndSplitPath(path: String) = {
    val q = path.indexOf('?')
    if (q > 0)
      splitPathWithoutLeadingSlash(path.substring(1, q))
    else
      splitPathWithoutLeadingSlash(path.substring(1))
  }

  private def splitPathWithoutLeadingSlash(path: String) = if (path.length > 0) List() ++ path.split('/') else Nil
}