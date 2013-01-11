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

import java.nio.charset.Charset
import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.HttpClient
import java.nio.ByteBuffer
import uk.co.bigbeeconsultants.http.util.Splitter

/**
 * Defines the outline of a response body. This may or may not be buffered in memory or streamed directly
 * to where it is needed, depending on the implementation.
 *
 * When the content is textual, instances may be useful as an Iterable[String], providing a way of
 * accessing the text line by line.
 */
trait ResponseBody extends Iterable[String] {
  /**
   * Gets the content type and encoding of the response.
   */
  def contentType: MediaType

  /**
   * Gets the length of the content as the number of bytes received. If compressed on the wire,
   * this will be the uncompressed length so will differ from the 'Content-Length' header. Because
   * 'contentLength' represents the byte array size, 'asString.length' will probably compute a different value.
   */
  def contentLength = 0

  /**
   * Gets the body as an array of raw bytes if available. By default, this merely returns an empty array,
   * which may not be much use.
   */
  def asBytes: Array[Byte] = new Array[Byte](0)

  /**
   * Gets the body as a string, if available. If the data is binary, this method always returns a blank string.
   */
  def asString: String = ""

  /**
   * Gets the body as a string split into lines, if available. If the data is binary, this method always returns
   * an empty iterator.
   */
  def iterator: Iterator[String] = if (isTextual && asString.length > 0) new Splitter(asString, '\n') else Nil.iterator

  /**
   * Tests whether this response body can be represented as text, or whether the data is binary.
   */
  def isTextual = true
}


/**
 * Provides an empty Body implementation.
 */
final class EmptyResponseBody(val contentType: MediaType) extends ResponseBody {
  override def toString = asString
}


/**
 * Provides a body implementation based simply on a string.
 */
final class StringResponseBody(val bodyText: String, val contentType: MediaType) extends ResponseBody {

  @deprecated
  def this(contentType: MediaType, bodyText: String) = this(bodyText, contentType)

  /**
   * Converts the body of the response into an array of bytes.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   */
  override def asBytes: Array[Byte] = {
    val charset = contentType.charset.getOrElse (HttpClient.UTF8)
    val buf = Charset.forName (charset).encode (bodyText)
    val bytes = new Array[Byte](buf.limit ())
    buf.get (bytes, 0, buf.limit ())
    bytes
  }

  /**
   * Get the body of the response as a string.
   */
  override def asString: String = bodyText

  override def toString = asString

  override lazy val contentLength = asBytes.length
}
