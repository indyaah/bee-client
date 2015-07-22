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

package uk.co.bigbeeconsultants.http.util

import javax.servlet.http.HttpServletRequest
import java.{util => ju}
import java.util.Locale
import uk.co.bigbeeconsultants.http.header.Headers
import uk.co.bigbeeconsultants.http.url.Href

/**
 * Provides a testing implementation of HttpServletRequest that doesn't suffer from the pre-Java-generics
 * type-conversion problems that plague Mockito in Scala. Typically, java.util.Enumeration cannot be
 * created in a way that satisfies the Scala compiler
 */
@deprecated("This will be removed from future versions.", "0.25.1")
class StubHttpServletRequest extends HttpServletRequest {
  var authType = ""
  var characterEncoding = ""
  var contentLength = 0
  var contentType = ""
  var contextPath = "/context"
  var protocol = "HTTP/1.1"
  var scheme = "http"
  var method = "GET"
  var serverName = "localhost"
  var serverPort = -1
  var remoteAddr = ""
  var remoteHost = ""
  var remotePort = 0
  var remoteUser = ""
  var localAddr = ""
  var localName = ""
  var localPort = 0
  var requestURI = "/context/x/y/z"
  var requestURL = new StringBuffer
  var queryString = "a=1"
  var pathInfo = "/x/y/z"
  var pathTranslated = ""
  var servletPath = ""
  var secure = false
  val locale = Locale.getDefault

  val attributes = new ju.LinkedHashMap[String, AnyRef]
  val parameters = new ju.LinkedHashMap[String, String]
  val headers = new ju.LinkedHashMap[String, ju.List[String]]

  def getAttribute(p1: String) = attributes.get(p1)

  def getAttributeNames = ju.Collections.enumeration(attributes.keySet())

  def getCharacterEncoding = characterEncoding

  def setCharacterEncoding(ce: String) {
    characterEncoding = ce
  }

  def getContentLength = contentLength

  def getContentType = contentType

  def getInputStream = null

  def getParameter(key: String) = parameters.get(key)

  def getParameterNames = ju.Collections.enumeration(parameters.keySet())

  def getParameterValues(p1: String) = null

  def getParameterMap = parameters

  def getProtocol = protocol

  def getScheme = scheme

  def getServerName = serverName

  def getServerPort = serverPort

  def getReader = null

  def getRemoteAddr = remoteAddr

  def getRemoteHost = remoteHost

  def setAttribute(key: String, value: AnyRef) {
    attributes.put(key, value)
  }

  def removeAttribute(key: String) {
    attributes.remove(key)
  }

  def getLocale = locale

  def getLocales = null

  def isSecure = secure

  def getRequestDispatcher(p1: String) = null

  def getRealPath(p1: String) = ""

  def getRemotePort = remotePort

  def getLocalName = localName

  def getLocalAddr = localAddr

  def getLocalPort = localPort

  def getAuthType = authType

  def getCookies = null

  def getDateHeader(p1: String) = 0L

  def getHeader(key: String) = headers.get(key).get(0)

  def getHeaders(key: String) = ju.Collections.enumeration(headers.get(key))

  def getHeaderNames = ju.Collections.enumeration(headers.keySet())

  def getIntHeader(p1: String) = 0

  def getMethod = method

  def getPathInfo = pathInfo

  def getPathTranslated = pathTranslated

  def getContextPath = contextPath

  def getQueryString = queryString

  def getRemoteUser = remoteUser

  def isUserInRole(p1: String) = false

  def getUserPrincipal = null

  def getRequestedSessionId = ""

  def getRequestURI = requestURI

  def getRequestURL = requestURL

  def getServletPath = servletPath

  def getSession(p1: Boolean) = null

  def getSession = null

  def isRequestedSessionIdValid = false

  def isRequestedSessionIdFromCookie = false

  def isRequestedSessionIdFromURL = false

  def isRequestedSessionIdFromUrl = false

  def copyFrom(url: Href) = {
    require (url.isURL)
    require (url.path.size > 1)
    val endpoint = url.endpoint.get
    scheme = endpoint.scheme
    serverName = endpoint.host
    serverPort = endpoint.port.getOrElse(-1)
    contextPath = "/" + url.path.head
    requestURI = url.path.toString
    queryString = url.query.getOrElse("")
    this
  }

  def copyFrom(hdrs: Headers) = {
    for (h <- hdrs.list) {
      var list = headers.get(h.name)
      if (list == null) {
        list = new ju.ArrayList()
        headers.put(h.name, list)
      }
      list.add(h.value)
    }
    this
  }
}
