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

package uk.co.bigbeeconsultants.http.servlet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import uk.co.bigbeeconsultants.http.header._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import java.io.InputStream
import uk.co.bigbeeconsultants.http.response.{Status, ResponseBuilder}
import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.request.{URLMapper, Request, RequestBody}
import uk.co.bigbeeconsultants.http.url.{Path, PartialURL}
import scala.Some

/**
 * Adapts HTTP Servlet request objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 */
class HttpServletRequestAdapter(req: HttpServletRequest,
                                urlMapper: URLMapper = URLMapper.noop) {

  def requestURL: PartialURL = {
    val port = if (req.getServerPort < 0) None else Some(req.getServerPort)
    new PartialURL(Option(req.getScheme), Option(req.getServerName), port,
      Path(req.getRequestURI), None, Option(req.getQueryString))
  }

  def url: PartialURL = {
    urlMapper.mapToDownstream(requestURL)
  }

  def headers: Headers = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    new Headers(enumerationAsScalaIterator(req.getHeaderNames).map {
      headerName: Any =>
        new Header(headerName.toString, req.getHeader(headerName.toString))
    }.toList)
  }

  def requestBody: RequestBody = {
    val contentType = req.getContentType
    val mediaType = if (contentType != null) MediaType(contentType) else MediaType.TEXT_PLAIN
    RequestBody(req.getInputStream, mediaType)
  }
}


/**
 * Adapts HTTP Servlet response objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 * @param resp the response to be created
 * @param urlMapper an optional mapper that is applied to every line of the body content.
 *                This is used only when the content is treated as text (see `condition`)
 * @param condition an optional condition that limits when the rewrite function will be used. By default,
 *                  any media type that returns `true` for `isTextual` will be processed as text and this
 *                  may imply transcoding. Otherwise the response body is processed as binary data.
 */
class HttpServletResponseAdapter(resp: HttpServletResponse,
                                 urlMapper: URLMapper = URLMapper.noop,
                                 condition: (MediaType) => Boolean = (mt) => mt.isTextual) extends ResponseBuilder {

  private[this] var _request: Request = _
  private[this] var _status: Status = _
  private[this] var _mediaType: Option[MediaType] = _
  private[this] var _headers: Headers = _

  def setResponseHeaders(headers: Headers) {
    for (header <- headers.list) {
      header.name match {
        case LOCATION.name => resp.setHeader(header.name, urlMapper.rewriteResponse(header.value))
        case CONTENT_LENGTH.name => // do not keep because the size may change when URLs are fixed up
        case _ => resp.setHeader(header.name, header.value)
      }
    }
  }

  def setResponseCookies(jar: CookieJar) {
    for (cookie <- jar.cookies) {
      val changedCookie = cookie.copy(path = urlMapper.rewriteResponse(cookie.path))
      resp.addCookie(changedCookie.asServletCookie)
    }
  }

  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType],
                      headers: Headers, cookies: Option[CookieJar], inputStream: InputStream) {
    try {
      _request = request
      _status = status
      _mediaType = mediaType
      _headers = headers

      resp.setStatus(status.code, status.message)

      setResponseHeaders(headers)

      if (cookies.isDefined) {
        setResponseCookies(cookies.get)
      }

      if (mediaType.isDefined && condition(mediaType.get)) {
        HttpUtil.copyText(inputStream, resp.getOutputStream, mediaType.get.charsetOrUTF8, urlMapper.rewriteResponse)
      }
      else {
        HttpUtil.copyBytes(inputStream, resp.getOutputStream)
      }

    } finally {
      if (inputStream != null) inputStream.close()
      resp.getOutputStream.close()
    }
  }

  def request = _request

  def status = _status

  def mediaType = _mediaType

  def headers = _headers
}
