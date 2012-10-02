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

import java.util.regex.Pattern
import uk.co.bigbeeconsultants.http.url.{Path, PartialURL}

/**
 * Provides a convenient decoupling between an application's upstream hrefs and the downstream URLs that need to
 * be requested.
 *
 * 'Downstream' implies the outgoing request that the HTTP client will make or has made.

 * 'Upstream' implies the return response that the HTTP client will receive or has received.
 *
 * This base class simply passes through all requests unchanged. It simply provides a hook for subclassing.
 */
class URLMapper {
  def mapToDownstream(href: String): PartialURL = mapToDownstream(PartialURL(href))

  def mapToDownstream(href: PartialURL): PartialURL = href

  def mapToUpstream(href: String): PartialURL = mapToUpstream(PartialURL(href))

  def mapToUpstream(href: PartialURL): PartialURL = href

  def rewriteRequest(body: String): String = body

  def rewriteResponse(body: String): String = body
}

/**
 * Extends [[uk.co.bigbeeconsultants.http.request.URLMapper]] to provide a convenient mapping from one URL
 * namespace to another.
 */
class DefaultURLMapper(upstreamBase: PartialURL, downstreamBase: PartialURL) extends URLMapper {
  require(upstreamBase.isURL == downstreamBase.isURL,
    upstreamBase + " and " + downstreamBase + " : upstream and downstream sides must be compatible.")

  private val downstreamBaseStr = downstreamBase.toString
  private val downstreamPath = downstreamBase.path.toString
  private val upstreamBaseStr = upstreamBase.toString
  private val upstreamPath = upstreamBase.path.toString

  private val compiledDownstreamAbs = Pattern.compile(downstreamBaseStr)
  private val compiledDownstreamRel = Pattern.compile(downstreamPath)
  private val compiledUpstreamAbs = Pattern.compile(upstreamBaseStr)
  private val compiledUpstreamRel = Pattern.compile(upstreamPath)

  private def map(from: PartialURL, to: PartialURL, href: PartialURL): PartialURL = {
    val scheme = if (to.scheme.isDefined) to.scheme else href.scheme
    val host = if (to.host.isDefined) to.host else href.host
    val port = if (to.port.isDefined) to.port else href.port
    val path = new Path(true, to.path.segments ++ href.path.segments.drop(from.path.size))
    new PartialURL(scheme, host, port, path, href.fragment, href.query)
  }

  override def mapToDownstream(href: PartialURL): PartialURL = {
    map(upstreamBase, downstreamBase, href)
  }

  override def mapToUpstream(href: PartialURL): PartialURL = {
    map(downstreamBase, upstreamBase, href)
  }

  private def rewrite2(fromAbs: Pattern, fromRel: Pattern, toAbs: String, toRel: String, content: String): String = {
    val pass1 = if (upstreamBase.isURL) fromAbs.matcher(content).replaceAll(toAbs) else content
    fromRel.matcher(pass1).replaceAll(toRel)
  }

  override def rewriteRequest(content: String): String = {
    rewrite2(compiledUpstreamAbs, compiledUpstreamRel, downstreamBaseStr, downstreamPath, content)
  }

  override def rewriteResponse(content: String): String = {
    rewrite2(compiledDownstreamAbs, compiledDownstreamRel, upstreamBaseStr, upstreamPath, content)
  }
}

