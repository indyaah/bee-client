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

package uk.co.bigbeeconsultants.lhc.response

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import uk.co.bigbeeconsultants.lhc.header.MediaType
import uk.co.bigbeeconsultants.lhc.HttpClient
import uk.co.bigbeeconsultants.lhc.Util

/**
 * Defines the outline of a response body. This may or may not be buffered in memory or streamed directly
 * to where it is needed, depending on the implementation.
 */
trait Body {
  /**Implementors will provide the means via this method for the response data to be consumed. */
  def receiveData(contentType: MediaType, inputStream: InputStream) {}

  /**Gets the content type and encoding of the response. */
  def contentType: MediaType

  /**
   * Gets the body as a string if available. By default, this merely returns toString(), which may not be
   * much use.
   */
  def asString: String = toString
}


/**
 * Provides a Body implementation that caches a response in a ByteBuffer. This is also available
 * as a string without much performance penalty. However, take care because the memory footprint will be large
 * when dealing with large volumes of response data.
 */
trait CachedBody extends Body {
  def asBytes: Array[Byte]
}


/**
 * Provides a Body implementation that caches a response in a ByteBuffer. This is also available
 * as a string without much performance penalty. However, take care because the memory footprint will be large
 * when dealing with large volumes of response data.
 */
final class BodyCache(val contentType: MediaType, byteData: ByteBuffer) extends CachedBody {

  private var converted: String = null

  private def convertToString = {
    val charset = contentType.charset.getOrElse(HttpClient.UTF8)
    val string = Charset.forName(charset).decode(byteData).toString
    byteData.rewind
    string
  }

  /**
   * Get the body of the response as a string.
   */
  override def asString: String = {
    if (converted == null) {
      converted = convertToString
    }
    converted
  }

  /**
   * Get the body of the response as an array of bytes.
   */
  def asBytes: Array[Byte] = byteData.array()
}


/**
 * Provides a Body implementation that copies the whole response into a ByteBuffer. This is also available
 * as a string without much performance penalty. However, take care because the memory footprint will be large
 * when dealing with large volumes of response data.
 */
final class BufferedBody extends CachedBody {
  private var cache: BodyCache = null

  override def receiveData(contentType: MediaType, inputStream: InputStream) {
    cache = new BodyCache(contentType, Util.copyToByteBufferAndClose(inputStream))
  }

  def contentType: MediaType = if (cache != null) cache.contentType else null

  /**
   * Get the body of the response as a string.
   */
  override def asString: String = if (cache != null) cache.asString else null

  /**
   * Get the body of the response as an array of bytes.
   */
  def asBytes: Array[Byte] = if (cache != null) cache.asBytes else null
}

object BufferedBody {
  implicit def asString(body: BufferedBody) = body.asString
}