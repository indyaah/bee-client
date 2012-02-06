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

package uk.co.bigbeeconsultants.lhc

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Defines the outline of a response body. This may or may not be buffered in memory or streamed directly
 * to where it is needed, depending on the implementation.
 */
trait ResponseBody {
  /**Implementors will provide the means via this method for the response data to be consumed. */
  def receiveData (contentType: MediaType, inputStream: InputStream)

  /** Gets the content type and encoding of the response. */
  def contentType: MediaType

  /**
   * Gets the body as a string if available. By default, this merely returns toString(), which may not be
   * much use.
   */
  def body: String = toString
}


/**
 * Provides a ResponseBody implementation that copies the whole response into a ByteBuffer. This is available
 * as a string without much performance penalty. However, take care because the memory footprint will be large
 * when dealing with large volumes of response data.
 */
class BufferedResponseBody extends ResponseBody {
  private var _contentType: MediaType = null
  private var byteData: ByteBuffer = null

  def receiveData(contentType: MediaType, inputStream: InputStream) {
    _contentType = contentType
    byteData = Util.copyToByteBufferAndClose(inputStream)
  }

  private def getBody = {
    val charset = _contentType.charset.getOrElse(HttpClient.defaultCharset)
    val body = Charset.forName(charset).decode(byteData).toString
    byteData.rewind
    body
  }

  def contentType: MediaType = _contentType

  /**
   * Get the body of the response as a plain string.
   * @return body
   */
  override lazy val body: String = getBody

  /**
   * Get the body of the response as an array of bytes.
   * @return body bytes
   */
  def bodyAsBytes: Array[Byte] = byteData.array()
}