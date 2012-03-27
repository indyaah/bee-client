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
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.{Header, Headers}
import uk.co.bigbeeconsultants.http.response.Response

/**
 * Adapts HTTP Servlet request and response objects to the Light Http Client API. This allows a variety of
 * solutions such as proxying to be implemented easily.
 */
class HttpServletAdapter {

  def getRequestBody(req: HttpServletRequest): RequestBody = {
//    request.RequestBody()
//    req.getInputStream
    null
  }

  def convertRequestHeaders(req: HttpServletRequest): Headers = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    new Headers(enumerationAsScalaIterator(req.getHeaderNames).map {
      headerName: Any =>
        new Header(headerName.toString, req.getHeader(headerName.toString))
    }.toList)
  }

  def copyResponse(response: Response, resp: HttpServletResponse) = {
    for (header <- response.headers.list) {
      resp.setHeader(header.name, header.value)
    }
    response.body.asBytes
  }
}
