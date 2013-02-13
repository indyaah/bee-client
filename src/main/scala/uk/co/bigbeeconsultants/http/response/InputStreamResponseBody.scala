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
import header.{Headers, MediaType}
import request.Request
import java.io.InputStream
import java.nio.charset.Charset

/**
 * Provides a body implementation that holds the HTTP server's input stream as obtained from the HttpURLConnection.
 * This can easily be converted into a ByteBufferResponseBody.
 *
 * It is not safe to share instances between threads.
 */
final class InputStreamResponseBody(request: Request, status: Status, mediaType: Option[MediaType],
                                    headers: Headers, stream: InputStream)
  extends ResponseBody {

  private var state: Either[InputStream, ByteBufferResponseBody] = Left(stream)

  def isBuffered = state.isRight

  /**
   * Gets the response body in a buffer. This consumes the data from the input stream, which cannot subsequently
   * be used therefore.
   * @return the response data stored in a buffer
   */
  override def toBufferedBody: ResponseBody = {
    if (state.isLeft)
      state = Right(ByteBufferResponseBody(request, status, mediaType, stream, headers))
    state.right.get
  }

  /**
   * Gets the length of the content as the number of bytes received, if known. This requires that the response has
   * been buffered. Otherwise, an illegal state is encountered; avoid this by testing via `isBuffered`.
   */
  override def contentLength = {
    if (state.isLeft)
      throw new IllegalStateException("The actual content length has not yet been determined.")
    state.right.get.contentLength
  }

  /**
   * Gets the content type and encoding of the response. In the unbuffered state, this may not have been supplied
   * by the HTTP server so may not yet be known, in which case the default is "application/octet-stream".
   */
  override def contentType = {
    if (state.isLeft)
      mediaType getOrElse MediaType.APPLICATION_OCTET_STREAM
    else
      state.right.get.contentType
  }

  /**
   * Gets a proxy for the HTTP resposne input stream in which each line is transformed using a specified function.
   * @return the proxied input stream
   */
  def rawStream: InputStream = {
    if (state.isRight)
      throw new IllegalStateException("Cannot access HTTP input stream once the content has been buffered.")
    state.left.get
  }

  /**
   * Gets a proxy for the HTTP resposne input stream in which each line is transformed using a specified function.
   * @param lineFilter the function to be applied to each line.
   * @return the proxied input stream
   */
  def transformedStream(lineFilter: TextFilter): InputStream = {
    val charset = Charset.forName(contentType.charsetOrUTF8)
    new LineFilterInputStream(rawStream, lineFilter, charset)
  }

  def close() {
    stream.close()
  }

  override def toString() = if (state.isLeft) "(unbuffered input stream)" else state.right.get.asString
}
