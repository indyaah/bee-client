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

package uk.co.bigbeeconsultants.http.response

import uk.co.bigbeeconsultants.http.header.Headers
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.header.MediaType
import java.io.InputStream

/**
 * Represents a HTTP response. This is essentially immutable, although the implementation of
 * the response body may vary.
 */
case class Response(request: Request, status: Status, body: ResponseBody, headers: Headers)


/**
 * Defines how responses will be handled. The 'standard' implementation is BufferedResponseFactory,
 * which returns responses buffered in byte arrays (and also strings).
 * @see BufferedResponseFactory
 */
trait ResponseFactory {
  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType], headers: Headers, stream: InputStream)

  def response: Option[Response] = None
}


/**
 * Provides a response factory implementation that returns responses buffered in byte
 * arrays (and also strings), using ByteBufferResponseBody.
 * @see ByteBufferResponseBody
 */
class BufferedResponseFactory extends ResponseFactory {
  // with Logging {
  private var _response: Option[Response] = None

  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType], headers: Headers, stream: InputStream) {
    val body = new ByteBufferResponseBody (mediaType.getOrElse (MediaType.APPLICATION_OCTET_STREAM), stream)
    _response = Some (new Response (request, status, body, headers))
    //    logger.debug((_response.get.toString))
  }

  override def response = _response
}
