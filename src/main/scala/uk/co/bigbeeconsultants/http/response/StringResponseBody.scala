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
 * Provides a body implementation based simply on a string.
 *
 * This is immutable and therefore can be safely shared between threads.
 */
case class StringResponseBody(bodyText: String, contentType: MediaType) extends ResponseBody {

  @deprecated
  def this(contentType: MediaType, bodyText: String) = this(bodyText, contentType)

  /** Always true. */
  override def isBuffered = true

  /**
   * Returns `this`.
   */
  override def toBufferedBody = this

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
  override def asString = bodyText

  override def toString() = asString

  override lazy val contentLength = asBytes.length
}
