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
import java.io._
import java.nio.charset.Charset

/**
 * Provides a body implementation that holds the HTTP server's input stream as obtained from the HttpURLConnection.
 * This provides access to the underlying InputStream, with or without a filtering function to transform the text,
 * Also, is can also easily be iterated over as a sequence of strings.
 *
 * Alternatively, it can be converted into a [[uk.co.bigbeeconsultants.http.response.ByteBufferResponseBody]]
 * (which is simpler to use) if the whole body is required as a block of bytes or a string.
 *
 * It is not safe to share instances between threads.
 *
 * The socket input stream *must* be closed by the calling code. This happens automatically when all the data has
 * been consumed from the socket, but if it is necessary to give up immediately or part-way through, it is necessary
 * to ensure that `rawStream.close()` is invoked (usually in a `finally` block). Conversion `toBufferedBody` is
 * another way to ensure the input stream gets closed.
 */
final class InputStreamResponseBody(request: Request, status: Status, mediaType: Option[MediaType],
                                    headers: Headers, stream: InputStream)
  extends ResponseBody {

  private var bufferedBody: Option[ByteBufferResponseBody] = None

  /** Tests whether the content has been buffered yet. */
  override def isBuffered = bufferedBody.isDefined

  /**
   * Gets the response body in a buffer. This consumes the data from the input stream, which cannot subsequently
   * be used here therefore. Any data that has already been consumed will not be included in the resulting buffer.
   * @return the response data stored in a buffer
   */
  override def toBufferedBody: ResponseBody = {
    if (bufferedBody.isEmpty)
      bufferedBody = Some(ByteBufferResponseBody(request, status, mediaType, stream, headers))
    bufferedBody.get
  }

  /**
   * Throws an illegal state exception. The actual content length is only known once the response body has been
   * buffered. Therefore, use `toBufferedBody.contentLength` instead.
   */
  override def contentLength: Int = {
    throw new IllegalStateException("The content length has not yet been determined.")
  }

  /**
   * Gets the content type and encoding of the response. In the unbuffered state, this may not have been supplied
   * by the HTTP server so may not yet be known, in which case the default is "application/octet-stream".
   */
  override def contentType = {
    if (bufferedBody.isEmpty)
      mediaType getOrElse MediaType.APPLICATION_OCTET_STREAM
    else
      bufferedBody.get.contentType
  }

  /**
   * Throws an illegal state exception. The actual content is only known once the response body has been
   * buffered. Therefore, use `toBufferedBody.asBytes` instead.
   */
  override def asBytes: Array[Byte] = {
    throw new IllegalStateException("The content has not yet been buffered.")
  }

  /**
   * Gets the HTTP response input stream. If you read from this stream, it would be unwise later to buffer
   * the body using `toBufferedBody`, because only the remainder would get buffered. Also, once a buffered
   * body has been obtained, this method must not be used (an exception will be thrown if it is attempted).
   * @return the proxied input stream
   */
  def rawStream: InputStream = {
    if (bufferedBody.isDefined)
      throw new IllegalStateException("Cannot access HTTP input stream once the content has been buffered.")
    stream
  }

  /**
   * Gets a proxy for the HTTP resposne input stream in which each line is transformed using a specified function.
   * Be careful about the potential impact on performance of your filter on line-by-line processing; you may need to
   * measure this approach in comparison with other alternatives.
   * @param lineFilter the function to be applied to each line.
   * @return the proxied input stream
   */
  def transformedStream(lineFilter: TextFilter): InputStream = {
    val charset = Charset.forName(contentType.charsetOrUTF8)
    new LineFilterInputStream(rawStream, lineFilter, charset)
  }

  /**
   * Gets the body as a string split into lines of text, if possible. If the data is binary, this method always returns
   * an empty iterator.
   *
   * If the content is textual, this method will throw an illegal state exception if `toBufferedBody` has already
   * been used.
   */
  @throws(classOf[IOException])
  override def iterator = {
    if (!isTextual)
      Nil.iterator
    else
      new Iterator[String] {
        val reader = new BufferedReader(new InputStreamReader(rawStream, contentType.charsetOrUTF8))
        var line: String = _

        lookAhead()

        @throws(classOf[IOException])
        private def lookAhead() {
          line = reader.readLine()
          if (line == null) reader.close()
        }

        def hasNext = line != null

        @throws(classOf[IOException])
        def next() = {
          val next = line
          lookAhead()
          next
        }
      }
  }

  @throws(classOf[IOException])
  def close() {
    if (bufferedBody.isEmpty)
      stream.close()
  }

  /**
   * Always returns a blank string because the data is as-yet un-buffered. Instead, first use `toBufferedBody` and call
   * `asString` on the result.
   */
  override def asString = ""

  override def toString() = if (bufferedBody.isEmpty) "(unbuffered input stream)" else bufferedBody.get.asString
}
