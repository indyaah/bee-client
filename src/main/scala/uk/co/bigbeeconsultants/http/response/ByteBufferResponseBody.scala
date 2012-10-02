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

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.io.InputStream
import java.net.URL
import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.url.PartialURL

/**
 * Provides a body implementation that copies the whole response from the response input stream into a ByteBuffer.
 * This is also available as a string without much performance penalty.
 *
 * If no media type was specified in the response from the server, this class attempts to guess a sensible fallback.
 * This works via the following steps: 1. Empty data is application/octet-stream. 2. Data containing control codes is
 * application/octet-stream. 3. Data starting with '<' is text/html. 4. Otherwise text/plain is assumed.
 *
 * Take care because the memory footprint will be large when dealing with large volumes of response data.
 */
final class ByteBufferResponseBody(requestUrl: Option[URL],
                                   optionalContentType: Option[MediaType],
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

  lazy val contentType: MediaType = optionalContentType.getOrElse(guessMediaTypeFromContent)

  // edge case for undefined content type
  private def guessMediaTypeFromContent: MediaType = {
    val extension = requestUrl.flatMap(PartialURL(_).extension)
    if (extension.isDefined) {
      MimeTypeRegistry.table.get(extension.get).getOrElse(guessMediaTypeFromBodyData)
    } else {
      guessMediaTypeFromBodyData
    }
  }

  def guessMediaTypeFromBodyData: MediaType = {
    if (byteData.limit() == 0) {
      APPLICATION_OCTET_STREAM

    } else {
      var maybeHtml = byteData.get(0) == '<'
      var maybeText = true
      var i = 0
      while (i < byteData.limit()) {
        val bi = byteData.get(i).toInt
        if (Character.isISOControl(bi) && !Character.isWhitespace(bi)) {
          maybeText = false
          maybeHtml = false
          i = byteData.limit() // break
        }
        i += 1
      }
      if (maybeHtml) TEXT_HTML else if (maybeText) TEXT_PLAIN else APPLICATION_OCTET_STREAM
    }
  }

  /**
   * Tests whether this response body can be represented as text, or whether the data is binary.
   */
  override def isTextual = contentType.isTextual

  /**
   * Get the body of the response as an array of bytes.
   */
  override def asBytes: Array[Byte] = {
    assert(byteData.hasArray)
    byteData.array()
  }

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
