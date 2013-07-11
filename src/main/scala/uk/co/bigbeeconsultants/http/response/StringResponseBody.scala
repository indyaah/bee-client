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
import uk.co.bigbeeconsultants.http.request._
import java.nio.ByteBuffer

/**
 * Provides a body implementation based simply on a string.
 *
 * This is immutable and therefore can be safely shared between threads.
 *
 * @param bodyText the actual body content (also known as response entity)
 * @param contentType the content type received from the webserver, if any, or else some suitable default
 */
case class StringResponseBody(bodyText: String,
                              contentType: MediaType) extends ResponseBody {

  @deprecated("Use the simpler constructor", "since v0.21.6")
  def this(request: Request, status: Status,
           bodyText: String, contentType: MediaType) = this(bodyText, contentType)

  /** Always false. */
  override def isUnbuffered = false

  /** Gets the body content in byte array form. */
  override lazy val toBufferedBody = new ByteBufferResponseBody(convertToBytes, contentType)

  /** Returns `this`. */
  override def toStringBody = this

  /**
   * Converts the body of the response into an array of bytes.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   */
  override def asBytes: Array[Byte] = toBufferedBody.asBytes

  /**
   * Gets the content as an input stream. Each time this is called, a new ByteArrayInputStream is returned. This
   * should be closed by the calling code when it has been finished with.
   */
  def inputStream = toBufferedBody.inputStream

  private def convertToBytes: ByteBuffer = {
    val charset = contentType.charset.getOrElse(HttpClient.UTF8)
    Charset.forName(charset).encode(bodyText)
  }

  /**
   * Get the body of the response as a string.
   */
  override def asString = bodyText

  override lazy val contentLength = asBytes.length
}
