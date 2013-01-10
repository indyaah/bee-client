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

import javax.servlet.http.HttpServletRequest
import uk.co.bigbeeconsultants.http._
import header._
import request.{StringRequestBody, RequestBody}
import url.{Endpoint, Path, Href}

/**
 * Adapts HTTP Servlet request objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 * @param textualBodyFilter an optional mutation that may be applied to every line of the body content.
 */
class HttpServletRequestAdapter(req: HttpServletRequest,
                                textualBodyFilter: Option[TextualBodyFilter] = None) {

  def url: Href = {
    val port = if (req.getServerPort < 0) None else Some(req.getServerPort)
    new Href(Endpoint(Option(req.getScheme), Option(req.getServerName), port),
      Path(req.getRequestURI), None, Option(req.getQueryString))
  }

  def headers: Headers = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    new Headers(enumerationAsScalaIterator(req.getHeaderNames).map {
      headerName: Any =>
        new Header(headerName.toString, req.getHeader(headerName.toString))
    }.toList)
  }

  /**
   * Gets the request body.
   * @return the request body which will be a low-footprint streaming implementation by default. However, it
   *         is possible to access the body by first caching it, e.g.
   *         {{{
   *           val adapter = new HttpServletRequestAdapter(req)
   *           val body = adapter.requestBody.cachedBody
   *           println(body.asString)
   *           val request = Request.post(url, Some(body))
   *           ...
   *         }}}
   */
  def requestBody: RequestBody = {
    val rawContentType = req.getContentType
    val contentType = if (rawContentType != null) MediaType(rawContentType) else MediaType.TEXT_PLAIN
    val streamRequestBody = RequestBody(req.getInputStream, contentType)

    if (textualBodyFilter.isDefined && textualBodyFilter.get.processAsText(contentType)) {
      val body = streamRequestBody.cachedBody.asString
      new StringRequestBody(body, contentType, textualBodyFilter.map(_.lineProcessor))
    }
    else {
      streamRequestBody
    }
  }
}
