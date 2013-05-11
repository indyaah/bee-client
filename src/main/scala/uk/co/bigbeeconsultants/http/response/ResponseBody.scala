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

import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.util.Splitter
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.Request
import java.io.IOException

/**
 * Defines the outline of a response body. This may or may not be buffered in memory or streamed directly
 * to where it is needed, depending on the implementation.
 *
 * When the content is textual, instances may be used as an Iterable[String], which provides a way of
 * accessing the text line by line.
 */
trait ResponseBody extends Iterable[String] {

  /**
   * Gets the content type and encoding of the response.
   */
  def contentType: MediaType

  /**
   * Tests whether this response body can be represented as text, or whether the data is binary.
   */
  final def isTextual = contentType.isTextual

  /**
   * Gets the length of the content as the number of bytes received. If compressed on the wire,
   * this will be the uncompressed length so will differ from the 'Content-Length' header. Because
   * 'contentLength' represents the byte array size, 'asString.length' will probably compute a different value.
   */
  def contentLength: Int

  /**
   * Tests whether the implementation has buffered the whole response body. If this returns false, `toBufferedBody`
   * provides an easy route to accumulate the entire body in a buffer.
   */
  def isBuffered: Boolean

  /**
   * Converts this response body into a buffered form if necessary. If it is already buffered, this method simply
   * returns `this`. Typically, a chain of conversions are provided by the implementation classes: if the
   * implementation is a `InputStreamResponseBody`, then this method will return a new `ByteBufferResponseBody`,
   * which will in turn return a new `StringResponseBody`, which will just return itself.
   */
  def toBufferedBody: ResponseBody

  /**
   * Gets the body as an array of raw bytes if available. By default, this merely returns an empty array,
   * which may not be much use.
   */
  def asBytes: Array[Byte]

  /**
   * Gets the body as a string, if available. If the data is as-yet unbuffered, this method always returns a blank
   * string.
   */
  def asString: String

  /**
   * Gets the response body as a string for diagnostic purposes.
   */
  override def toString() = asString

  /**
   * Gets the body as a string split into lines, if available. If the data is binary, this method always returns
   * an empty iterator.
   */
  def iterator: Iterator[String] = {
    val body = asString
    if (isTextual && body.length > 0) new Splitter(body, '\n') else Nil.iterator
  }

  /**
   * Closes the input stream that provides the data for this response. If the implementation buffers the body,
   * the input stream will have been closed automatically.
   */
  @throws(classOf[IOException])
  def close() {}

  // helper for the edge case for undefined content type
  private[response] def guessMediaTypeFromContent(request: Request, status: Status): MediaType = {
    val successfulGetUrlExtension =
      if (status.isSuccess && request.isGet) request.href.extension
      else None
    if (successfulGetUrlExtension.isDefined) {
      MimeTypeRegistry.table.get(successfulGetUrlExtension.get) getOrElse guessMediaTypeFromBodyData
    } else {
      guessMediaTypeFromBodyData
    }
  }

  // basic helper for cases where the media type must be guessed and body data is initially unavailable
  private[response] def guessMediaTypeFromBodyData: MediaType = APPLICATION_OCTET_STREAM
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides an empty `ResponseBody` implementation.
 */
final class EmptyResponseBody(val contentType: MediaType) extends ResponseBody {

  /** Returns `this`. */
  override def toBufferedBody = this

  /** Returns 0. */
  override def contentLength = 0

  /** Always true. */
  override def isBuffered = true

  /** Returns a zero-length array. */
  override val asBytes = new Array[Byte](0)

  /** Returns a blank string. */
  override def asString = ""
}
