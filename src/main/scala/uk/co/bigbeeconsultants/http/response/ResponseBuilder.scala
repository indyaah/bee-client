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

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.header.{CookieJar, Headers, MediaType}
import request.Request
import java.io.InputStream
import uk.co.bigbeeconsultants.http.util.{DiagnosticTimer, Duration}

/**
 * Defines how responses will be handled. The 'standard' implementation is BufferedResponseBuilder,
 * which returns responses buffered in byte arrays (and also strings). When an HTTP request is made,
 * captureResponse will usually be called once. However, during authentication, it may be called more
 * than once, all times except the last are transient; implementations need to handle this correctly.
 * @see BufferedResponseBuilder
 */
trait ResponseBuilder {
  /** Defines the method to be invoked when the response is first received. */
  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType],
                      headers: Headers, cookies: Option[CookieJar],
                      stream: InputStream, timer: DiagnosticTimer)

  /** Gets the response that was captured earlier. */
  def response: Option[Response] = None

  def setResponse(response: Response) {
    // no-op
  }

  /**
   * Gets the time taken from when the request was first sent across the network to when the response
   * was fully received (in the case of a buffered response) or just acquired (in the case of an unbuffered response).
   */
  def networkTimeTaken: Duration = Duration.Zero
  /** Gets the timer created before the request was first sent across the network. */
  def timer: Option[DiagnosticTimer] = None
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides a response builder implementation that returns responses buffered in byte
 * arrays (and also strings), using ByteBufferResponseBody.
 *
 * This is not thread safe so a new instance is required for every request.
 * @see ByteBufferResponseBody
 */
class BufferedResponseBuilder extends ResponseBuilder {

  private[response] var _response: Option[Response] = None
  private[response] var _networkTime: Duration = Duration.Zero
  private[response] var _timer: Option[DiagnosticTimer] = None

  def captureResponse(request: Request, status: Status, mediaType: Option[MediaType],
                      headers: Headers, cookies: Option[CookieJar],
                      stream: InputStream,
                      timer: DiagnosticTimer = new DiagnosticTimer) {
    _response = Some(captureBufferedResponse(request, status, mediaType, headers, cookies, stream))
    _networkTime = timer.duration
    _timer = Some(timer)
  }

  override def setResponse(response: Response) {
    _response = Some(response)
  }

  override def response = _response

  override def networkTimeTaken: Duration = _networkTime

  override def timer = _timer

  private[response] def captureBufferedResponse(request: Request, status: Status, mediaType: Option[MediaType],
                                                headers: Headers, cookies: Option[CookieJar],
                                                stream: InputStream): Response = {
    val body = ByteBufferResponseBody(request, status, mediaType, stream, headers)
    new Response(request, status, body, headers, cookies)
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides a response builder implementation that returns unbuffered responses along with the InputStream
 * that provides data from the origin HTTP server. This input stream *must* be closed by the calling code
 * to avoid connection leakage issues. The stream implementation makes this easier by automatically closing
 * the stream as soon as all the data has been consumed. This is an alternative to explicitly calling the
 * `close()` method on the stream; you can use either method.
 *
 * To make things easier, there are two different cases.
 *
 * 1. For 200 (OK) or 206 (partial content) only, an InputStreamResponseBody is returned, containing the
 * stream ready for use by the calling code, which *must* finally close the stream. As mentioned above, this
 * can either be done by explicitly calling `close()` or by consuming to the end of the stream, which in
 * this case will automatically close the stream.
 *
 * 2. For all responses other than 200 (OK) or 206 (partial content), a buffered body is returned in which the
 * stream has already been consumed and closed. Calling `close()` again is harmless but unnecessary.
 *
 * This is not thread safe so a new instance is required for every request.
 * @see InputStreamResponseBody
 */
final class UnbufferedResponseBuilder extends BufferedResponseBuilder {

  override def captureResponse(request: Request, status: Status, mediaType: Option[MediaType],
                               headers: Headers, cookies: Option[CookieJar],
                               stream: InputStream,
                               timer: DiagnosticTimer = new DiagnosticTimer) {
    val response =
      status.code match {
        case 200 | 206 =>
          captureUnbufferedResponse(request, status, mediaType, headers, cookies, stream)
        case _ =>
          captureBufferedResponse(request, status, mediaType, headers, cookies, stream)
      }
    _networkTime = timer.duration
    _response = Some(response)
    _timer = Some(timer)
  }

  private def captureUnbufferedResponse(request: Request, status: Status, mediaType: Option[MediaType],
                                        headers: Headers, cookies: Option[CookieJar],
                                        stream: InputStream): Response = {
    val body = new InputStreamResponseBody(request, status, mediaType, headers, stream)
    new Response(request, status, body, headers, cookies)
  }
}
