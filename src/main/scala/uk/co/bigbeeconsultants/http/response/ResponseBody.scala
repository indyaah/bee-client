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
import uk.co.bigbeeconsultants.http.util.LineSplitter
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.request.Request
import java.io.{InputStream, IOException}
import java.nio.charset.MalformedInputException

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
  final def isBuffered = !isUnbuffered

  /**
   * Tests whether the implementation has not yet buffered the whole response body. If this returns true,
   * `toBufferedBody` provides an easy route to accumulate the entire body in a buffer.
   */
  def isUnbuffered: Boolean

  /**
   * Converts this response body into a buffered form if necessary. If it is already buffered, this method simply
   * returns `this`.
   */
  def toBufferedBody: ResponseBody

  /**
   * Converts this response body into a buffered form as a string if necessary. If it is already a string,
   * this method simply returns `this`.
   */
  def toStringBody: ResponseBody

  /**
   * Gets the body as an array of raw bytes.
   */
  def asBytes: Array[Byte]

  /**
   * Converts the body to a string. An exception is thrown if the data is binary; use `isTextual` to
   * detect whether this is so.
   */
  def asString: String

  /**
   * Gets the response body as a string for diagnostic purposes.
   */
  override def toString(): String =
    if (contentType.isTextual) toStringBody.asString + " (" + contentType + ")"
    else "(binary data: " + contentLength + " bytes " + contentType + ")"

  /**
   * Gets the body as a string split into lines, if available.
   * This is only possible for textual content.
   * An IllegalStateException is thrown if the data is binary.
   */
  @throws(classOf[MalformedInputException])
  def iterator: Iterator[String] = {
    new LineSplitter(asString)
  }

  /** Gets the content as an input stream. */
  def inputStream: InputStream

  /**
   * Closes the input stream that provides the data for this response. If the implementation buffers the body,
   * the input stream will have been closed automatically.
   */
  @throws(classOf[IOException])
  def close() {}
}
