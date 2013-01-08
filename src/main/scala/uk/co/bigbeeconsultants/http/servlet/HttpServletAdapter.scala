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
import java.io.InputStream
import uk.co.bigbeeconsultants.http._
import header._
import header.HeaderName._
import response.{Status, ResponseBuilder}
import util.HttpUtil
import request.{StringSeqRequestBody, Request, RequestBody}
import url.{Endpoint, Path, PartialURL}

/**
 * Adapts HTTP Servlet request objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 * @param rewrite an optional mutation function that is applied to every line of the body content.
 *                This is used only when the content is treated as text (see `processAsText`)
 * @param processAsText an optional condition that limits when the rewrite function will be used. By default,
 *                      the response body is simply copied verbatim as binary data; therefore the rewrite
 *                      function is ignored. A suggested alternative value is `AllTextualMediaTypes`.
 */
class HttpServletRequestAdapter(req: HttpServletRequest,
                                rewrite: TextFilter = NoChangeTextFilter,
                                processAsText: MediaFilter = NoMediaTypes) {

  def url: PartialURL = {
    val port = if (req.getServerPort < 0) None else Some(req.getServerPort)
    new PartialURL(Endpoint(Option(req.getScheme), Option(req.getServerName), port),
      Path(req.getRequestURI), None, Option(req.getQueryString))
  }

  def headers: Headers = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    new Headers(enumerationAsScalaIterator(req.getHeaderNames).map {
      headerName: Any =>
        new Header(headerName.toString, req.getHeader(headerName.toString))
    }.toList)
  }

  def requestBody: RequestBody = {
    val rawContentType = req.getContentType
    val contentType = if (rawContentType != null) MediaType(rawContentType) else MediaType.TEXT_PLAIN
    val streamRequestBody = RequestBody(req.getInputStream, contentType)
    if (processAsText(contentType)) {
      val encoding = contentType.charsetOrElse(HttpClient.UTF8)
      val body = new String(streamRequestBody.cachedBody.asBytes, encoding)
      new StringSeqRequestBody(body.split('\n'), contentType, rewrite)
    }
    else streamRequestBody
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Adapts HTTP Servlet response objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 * @param resp the response to be created
 * @param rewrite an optional mutation function that is applied to every line of the body content.
 *                This is used only when the content is treated as text (see `processAsText`)
 * @param processAsText an optional condition that limits when the rewrite function will be used. By default,
 *                      the response body is simply copied verbatim as binary data; therefore the rewrite
 *                      function is ignored. A suggested alternative value is `AllTextualMediaTypes`.
 */
class HttpServletResponseAdapter(resp: HttpServletResponse,
                                 rewrite: TextFilter = NoChangeTextFilter,
                                 processAsText: MediaFilter = NoMediaTypes) extends ResponseBuilder {

  private[this] var _request: Request = _
  private[this] var _status: Status = _
  private[this] var _mediaType: Option[MediaType] = _
  private[this] var _headers: Headers = _

  def setResponseHeaders(headers: Headers) {
    for (header <- headers.list) {
      val value = if (header.name == LOCATION.name) rewrite(header.value) else header.value
      resp.setHeader(header.name, value)
    }
  }

  def setResponseCookies(jar: CookieJar) {
    for (cookie <- jar.cookies) {
      resp.addCookie(cookie.asServletCookie)
    }
  }

  def captureResponse(request: Request, status: Status, contentType: Option[MediaType],
                      headers: Headers, cookies: Option[CookieJar], inputStream: InputStream) {
    try {
      _request = request
      _status = status
      _mediaType = contentType
      _headers = headers

      resp.setStatus(status.code, status.message)

      setResponseHeaders(headers)

      if (cookies.isDefined) {
        setResponseCookies(cookies.get)
      }

      if (contentType.isDefined && processAsText(contentType.get)) {
        HttpUtil.copyText(inputStream, resp.getOutputStream, contentType.get.charsetOrUTF8, rewrite)
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
