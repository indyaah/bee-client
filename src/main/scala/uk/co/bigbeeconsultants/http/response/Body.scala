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

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.Util

/**
 * Defines the outline of a response body. This may or may not be buffered in memory or streamed directly
 * to where it is needed, depending on the implementation.
 */
trait Body {
  /**
   * Implementors may provide the means via this method for the response data to be consumed.
   */
  def receiveData(contentType: MediaType, inputStream: InputStream) {}

  /**
   * Gets the content type and encoding of the response.
   */
  def contentType: MediaType

  /**
   * Gets the body as an array of raw bytes if available. By default, this merely returns an empty array,
   * which may not be much use.
   */
  def asBytes: Array[Byte] = new Array[Byte](0)

  /**
   * Gets the body as a string if available. By default, this merely returns an empty string, which may not be
   * much use.
   */
  def asString: String = ""
}


/**
 * Provides an empty Body implementation.
 */
final class EmptyBody(val contentType: MediaType) extends Body {
  override def toString = asString
}


/**
 * Provides a Body implementation that holds a response in a ByteBuffer. This is also available
 * as a string without much performance penalty.
 */
final class ByteBufferBody(val contentType: MediaType, byteData: ByteBuffer) extends Body {

  private var converted: String = null

  private def convertToString = {
    val charset = contentType.charset.getOrElse(HttpClient.UTF8)
    val string = Charset.forName(charset).decode(byteData).toString
    byteData.rewind
    string
  }

  /**
   * Get the body of the response as an array of bytes.
   */
  override def asBytes: Array[Byte] =
    byteData.array()

  /**
   * Get the body of the response as a string.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   */
  override def asString: String = {
    if (converted == null) {
      converted = convertToString
    }
    converted
  }

  override def toString = asString
}


/**
 * Provides a Body implementation based simply on a string.
 */
final class StringBody(val contentType: MediaType, bodyText: String) extends Body {

  /**
   * Converts the body of the response into an array of bytes.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   */
  override def asBytes: Array[Byte] = {
    val charset = contentType.charset.getOrElse(HttpClient.UTF8)
    val buf = Charset.forName(charset).encode(bodyText)
    val bytes = new Array[Byte](buf.limit())
    buf.get(bytes, 0, buf.limit())
    bytes
  }

  /**
   * Get the body of the response as a string.
   */
  override def asString: String = bodyText

  override def toString = asString
}


/**
 * Provides a Body implementation that copies the whole response from an input stream into a ByteBuffer. This is
 * also available as a string without much performance penalty.
 * <p>
 * Take care because the memory footprint will be large when dealing with large volumes of response data.
 */
final class InputStreamBufferBody extends Body {
  private var cache: ByteBufferBody = null

  override def receiveData(contentType: MediaType, inputStream: InputStream) {
    cache = new ByteBufferBody(contentType, Util.copyToByteBufferAndClose(inputStream))
  }

  def contentType: MediaType = if (cache != null) cache.contentType else null

  /**
   * Get the body of the response as an array of bytes.
   */
  override def asBytes: Array[Byte] = if (cache != null) cache.asBytes else null

  /**
   * Get the body of the response as a string.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   */
  override def asString: String = if (cache != null) cache.asString else null

  override def toString = asString
}
