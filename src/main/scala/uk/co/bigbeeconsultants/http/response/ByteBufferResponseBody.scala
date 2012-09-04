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
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.io.InputStream
import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.HttpClient

/**
 * Provides a body implementation that copies the whole response from the response input stream into a ByteBuffer.
 * This is also available as a string without much performance penalty.
 * <p>
 * Take care because the memory footprint will be large when dealing with large volumes of response data.
 */
final class ByteBufferResponseBody(val contentType: MediaType,
                                   inputStream: InputStream,
                                   suppliedContentLength: Int = 0x10000) extends ResponseBody {

  private[this] val byteData: ByteBuffer = HttpUtil.copyToByteBufferAndClose(inputStream, suppliedContentLength)

  private[this] var converted: Option[String] = None

  private def convertToString = {
    if (contentType.isTextual) {
      val charset = contentType.charset.getOrElse(HttpClient.UTF8)
      val string = Charset.forName(charset).decode(byteData).toString
      byteData.rewind
      string
    }
    else {
      ""
    }
  }

  /**
   * Tests whether this response body can be represented as text, or whether the data is binary.
   */
  override def isTextual = contentType.isTextual

  /**
   * Get the body of the response as an array of bytes.
   */
  override def asBytes: Array[Byte] = byteData.array()

  /**
   * Get the body of the response as a string.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   * If the data is binary, this method always returns a blank string.
   */
  override def asString: String = {
    if (converted.isEmpty) {
      converted = Some(convertToString)
    }
    converted.get
  }

  override def toString = asString

  override lazy val contentLength = asBytes.length
}
