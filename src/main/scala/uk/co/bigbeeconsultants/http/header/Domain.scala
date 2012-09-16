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

import java.util.regex.Pattern
import java.net.{InetAddress, URL}

/**
 * Models a domain name as used in cookies.
 */
class Domain(val domain: String) {

  require (domain.length > 0)
  require (!domain.startsWith("."))

  lazy val parent: Option[Domain] = {
    if (isIpAddress) {
      None
    } else {
      val firstDot = domain.indexOf ('.')
      val lastDot = domain.lastIndexOf ('.')
      if (firstDot > 0 && lastDot > firstDot) {
        Some (Domain (domain.substring (firstDot + 1)))
      } else {
        None
      }
    }
  }

  def isIpAddress: Boolean = Domain.ipV4.matcher (domain).matches ()

  /**Tests whether this domain matches some URL. */
  def matches(url: URL) = {
    val host = url.getHost
    if (host == domain) {
      true
    } else if (host.length > domain.length) {
      host.endsWith (domain) &&
        host.charAt (host.length - domain.length - 1) == '.' &&
        !isIpAddress
    } else {
      false
    }
  }

  override def hashCode() = domain.hashCode()

  override def equals(obj: Any): Boolean = {
    if (obj == null) false
    else (obj.isInstanceOf[Domain] && this.domain == obj.asInstanceOf[Domain].domain)
  }

  override def toString = domain
}

/**
 * Provides support methods for managing domains.
 */
object Domain {
  /** Constructs a new domain based on the host in an URL. */
  def apply(url: URL): Domain = new Domain (url.getHost)

  /** Constructs a new domain based on a string, which may or may not start with '.'. */
  def apply(dom: String): Domain = if (dom.startsWith(".")) new Domain (dom.substring(1)) else new Domain(dom)

  //private def extractDomainFrom(url: URL) = HttpUtil$.divide(url.getAuthority, ':')._1

  /** Just "localhost" */
  val localhost = Domain("localhost")

  /** The hostname of the local machine */
  lazy val hostname = {
    new Domain(try {
      InetAddress.getLocalHost.getHostName
    } catch {
      case e: Exception => "localhost"
    })
  }

  private val ipV4 = Pattern.compile ("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+")
}