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
import uk.co.bigbeeconsultants.http.header.{MediaType, Header, Headers}
import java.io.InputStream
import uk.co.bigbeeconsultants.http.response.{Status, ResponseFactory}
import uk.co.bigbeeconsultants.http.Util
import uk.co.bigbeeconsultants.http.request.{SplitURL, Request, RequestBody}

/**
 * Adapts HTTP Servlet request objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 */
class HttpServletRequestAdapter(req: HttpServletRequest) {

  def url: SplitURL = {
    SplitURL(req.getScheme, req.getServerName, req.getServerPort, req.getRequestURI, null, req.getQueryString)
  }

  def headers: Headers = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    new Headers (enumerationAsScalaIterator (req.getHeaderNames).map {
      headerName: Any =>
        new Header (headerName.toString, req.getHeader (headerName.toString))
    }.toList)
  }

  def requestBody: RequestBody = {
    val contentType = req.getContentType
    val mediaType = if (contentType != null) MediaType (contentType) else MediaType.TEXT_PLAIN
    RequestBody (mediaType, req.getInputStream)
  }
}


/**
 * Adapts HTTP Servlet response objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 * @param resp the response to be created
 * @param rewrite an optional mutation function that is applied to every line of the body content.
 * @param condition an optional condition that limits when the rewrite function will be used
 */
class HttpServletResponseAdapter(resp: HttpServletResponse,
                                 rewrite: (String) => String = (x) => x,
                                 condition: (MediaType) => Boolean = (mt) => mt.isTextual) extends ResponseFactory {

  private[this] var _request: Request = _
  private[this] var _status: Status = _
  private[this] var _mediaType: Option[MediaType] = _
  private[this] var _headers: Headers = _

  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType], headers: Headers, inputStream: InputStream) {
    _request = request
    _status = status
    _mediaType = mediaType
    _headers = headers

    resp.setStatus (status.code, status.message)

    for (header <- headers.list) {
      resp.setHeader (header.name, header.value)
    }

    if (mediaType.isDefined && condition(mediaType.get)) {
      Util.copyText (inputStream, resp.getOutputStream, mediaType.get.charsetOrUTF8, rewrite)
    }
    else {
      Util.copyBytes (inputStream, resp.getOutputStream)
    }

    inputStream.close ()
    resp.getOutputStream.close()
  }

  def request = _request

  def status = _status

  def mediaType = _mediaType

  def headers = _headers
}
