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
import uk.co.bigbeeconsultants.http.request.{Request, RequestBody}
import uk.co.bigbeeconsultants.http.header.{MediaType, Header, Headers}
import java.io.InputStream
import uk.co.bigbeeconsultants.http.response.{CopyStreamResponseBody, Status, ResponseFactory}

/**
 * Adapts HTTP Servlet request objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 */
class HttpServletRequestAdapter(req: HttpServletRequest) {

  def headers: Headers = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    new Headers (enumerationAsScalaIterator (req.getHeaderNames).map {
      headerName: Any =>
        new Header (headerName.toString, req.getHeader (headerName.toString))
    }.toList)
  }

  def requestBody: RequestBody = {
    val mediaType = MediaType (req.getContentType)
    RequestBody(mediaType, req.getInputStream)
  }
}


/**
 * Adapts HTTP Servlet response objects to the Light Http Client API. This allows a variety of solutions such as
 * reverse proxying to be implemented easily.
 */
class HttpServletResponseAdapter(resp: HttpServletResponse) extends ResponseFactory {

  def captureResponse(request: Request, status: Status, mediaType: MediaType, headers: Headers, stream: InputStream) {
    resp.setStatus(status.code, status.message)

    for (header <- headers.list) {
      resp.setHeader (header.name, header.value)
    }

    val body = new CopyStreamResponseBody (resp.getOutputStream)
    body.receiveData (mediaType, stream)
    stream.close()
  }
}
