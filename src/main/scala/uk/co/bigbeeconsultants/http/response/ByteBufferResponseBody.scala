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
import uk.co.bigbeeconsultants.http.header.{HeaderName, Headers, MediaType}
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.request.Request

/**
 * Provides a body implementation that copies the whole response from the response input stream into a ByteBuffer.
 * This is also available as a string without much performance penalty.
 *
 * If no media type was specified in the response from the server, this class attempts to guess a sensible fallback.
 * This works via the following steps:
 * 1. If the request was a GET request and was successful and the URL ended with a file extension, use a MIME
 * type table to look up the content type.
 * 2. Empty data is application/octet-stream.
 * 3. Data containing control codes is application/octet-stream.
 * 4. Data starting with '<' is text/html.
 * 5. Otherwise text/plain is assumed.
 *
 * This implementation of [[uk.co.bigbeeconsultants.http.response.ResponseBody]] has the advantage of being simple
 * to use. However, take care because the memory footprint will be large when dealing with large volumes of
 * response data. As an alternative, consider [[uk.co.bigbeeconsultants.http.response.InputStreamResponseBody]].
 *
 * It is not safe to share instances between threads.
 */
final class ByteBufferResponseBody(request: Request,
                                   status: Status,
                                   optionalContentType: Option[MediaType],
                                   inputStream: InputStream,
                                   suppliedContentLength: Int = ByteBufferResponseBody.DefaultBufferSize)
  extends ResponseBody {

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
   * Returns a new StringResponseBody containing the text in this body in immutable form. The returned
   * object is safe for sharing between threads.
   */
  override def toBufferedBody = new StringResponseBody(asString, contentType)

  lazy val contentType: MediaType = optionalContentType getOrElse guessMediaTypeFromContent(request, status)

  private[response] override def guessMediaTypeFromBodyData: MediaType = {
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

  override def toString() = asString

  override lazy val contentLength = asBytes.length
}

//---------------------------------------------------------------------------------------------------------------------

object ByteBufferResponseBody {
  val DefaultBufferSize = 0x10000

  def apply(request: Request, status: Status, optionalContentType: Option[MediaType], inputStream: InputStream, headers: Headers) = {
    val length = headers.get(HeaderName.CONTENT_LENGTH).map(_.toNumber.toInt) getOrElse DefaultBufferSize
    new ByteBufferResponseBody(request, status, optionalContentType, inputStream, length)
  }
}