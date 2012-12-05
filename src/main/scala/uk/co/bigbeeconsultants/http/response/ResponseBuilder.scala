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

import uk.co.bigbeeconsultants.http.header.{HeaderName, CookieJar, Headers, MediaType}
import uk.co.bigbeeconsultants.http.request.Request
import java.io.InputStream
import java.net.URL

/**
 * Defines how responses will be handled. The 'standard' implementation is BufferedResponseBuilder,
 * which returns responses buffered in byte arrays (and also strings). When an HTTP request is made,
 * captureResponse will usually be called once. However, during authentication, it may be called more
 * than once, all times except the last are transient; implementations need to handle this correctly.
 * @see BufferedResponseBuilder
 */
trait ResponseBuilder {
  /**Defines the method to be invoked when the response is first received. */
  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType],
                      headers: Headers, cookies: Option[CookieJar], stream: InputStream)

  /**Gets the response that was captured earlier. */
  def response: Option[Response] = None
}


/**
 * Provides a response builder implementation that returns responses buffered in byte
 * arrays (and also strings), using ByteBufferResponseBody. This is not thread safe so
 * a new instance is required for every request.
 * @see ByteBufferResponseBody
 */
final class BufferedResponseBuilder extends ResponseBuilder {
  private var _response: Option[Response] = None

  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType],
                      headers: Headers, cookies: Option[CookieJar], stream: InputStream) {
    val body = new ByteBufferResponseBody(conditionalUrl(request, status), mediaType, stream, bufferSize(headers))
    _response = Some(new Response(request, status, body, headers, cookies))
  }

  override def response = _response

  private[response] def bufferSize(headers: Headers) = {
    headers.get(HeaderName.CONTENT_LENGTH).map(_.toNumber.toInt).getOrElse(BufferedResponseBuilder.DefaultBufferSize)
  }

  private[response] def conditionalUrl(request: Request, status: Status): Option[URL] = {
    if (status.isSuccess && request.isGet) Some(request.url) else None
  }
}

object BufferedResponseBuilder {
  val DefaultBufferSize = 1024
}