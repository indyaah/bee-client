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
import java.io.{ByteArrayInputStream, InputStream}
import uk.co.bigbeeconsultants.http.header.{HeaderName, Headers, MediaType}
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.request.Request

/**
 * Provides a body implementation that copies the whole response from the response input stream into a ByteBuffer.
 * The content is also available as a string without much performance penalty.
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
 * It is safe to share instances between threads provided no thread alters the byteArray data. A tricky
 * design decision was made here: the byte array is mutable because the alternatie would have been awkward
 * to use: immutable alternatives are not widely available, and would not be compatible with standard Java I/O APIs.
 * If required, use `toStringBody` to obtain the immutable `StringResponseBody` copy of the data.
 *
 * This class ensures that the socket input stream is always closed correctly, meaning the calling code is simpler
 * because it need not be concerned with cleaning up.
 *
 * @param byteArray the actual body content (also known as response entity)
 * @param contentType the content type received from the webserver, if any, or else some suitable default
 */
case class ByteBufferResponseBody(byteArray: Array[Byte], contentType: MediaType) extends ResponseBody {

  @deprecated("Use the simpler constructor", "since v0.21.6")
  def this(request: Request, status: Status, contentType: MediaType, byteArray: Array[Byte]) = this(byteArray, contentType)

  override val contentLength = byteArray.length

  private def convertToString = {
    if (!contentType.isTextual) {
      throw new IllegalStateException(contentType + ": it is not possible to convert this to text. " +
        "If you think this is wrong, please file a bug at https://bitbucket.org/rickb777/bee-client/issues")
    }

    val charset = contentType.charset.getOrElse(HttpClient.UTF8)
    val byteData = ByteBuffer.wrap(byteArray)
    Charset.forName(charset).decode(byteData).toString
  }

  /** Always false. */
  override def isUnbuffered = false

  /**
   * Gets the content as an input stream. Each time this is called, a new ByteArrayInputStream is returned. This
   * should be closed by the calling code when it has been finished with.
   */
  def inputStream = new ByteArrayInputStream(byteArray)

  /** Returns 'this'. */
  def toBufferedBody = this

  /**
   * Returns a new StringResponseBody containing the text in this body in immutable form. The returned
   * object is safe for sharing between threads.
   */
  lazy val toStringBody = new StringResponseBody(convertToString, contentType)

  /**
   * Get the body of the response as an array of bytes.
   */
  override def asBytes: Array[Byte] = byteArray

  /**
   * Get the body of the response as a string.
   * This uses the character encoding of the contentType, or UTF-8 as a default.
   * If the data is binary, this method always returns a blank string.
   */
  override def asString: String = toStringBody.asString
}

//---------------------------------------------------------------------------------------------------------------------

object ByteBufferResponseBody {
  val DefaultBufferSize = 0x10000
  val EmptyArray = new Array[Byte](0)

  def apply(request: Request,
            status: Status,
            optionalContentType: Option[MediaType],
            inputStream: InputStream,
            headers: Headers = Headers.Empty) = {
    val length = contentLength(headers)
    val byteArray = HttpUtil.copyToByteArrayAndClose(inputStream, length)
    val contentType = optionalContentType getOrElse guessMediaTypeFromContent(request, status, byteArray)
    new ByteBufferResponseBody(byteArray, contentType)
  }

  /**
   *
   * @param request if the content type is not initially known, the request URL may provide a hint based on the
   *                extension of the filename, if present.
   * @param status the request parameter is ignored if the status is not 200-OK.
   * @param optionalContentType the content type received from the webserver, if any
   * @param byteArray the actual body content (also known as response entity)
   */
  def apply(request: Request,
            status: Status,
            optionalContentType: Option[MediaType],
            byteArray: Array[Byte],
            headers: Headers) = {
    val contentType = optionalContentType getOrElse guessMediaTypeFromContent(request, status, byteArray)
    new ByteBufferResponseBody(byteArray, contentType)
  }

  private def contentLength(headers: Headers): Int = {
    val contentLength = headers.get(HeaderName.CONTENT_LENGTH)
    if (contentLength.isDefined) {
      val numValue = contentLength.get.toNumber
      if (numValue.isValid) numValue.toInt else 0
    }
    else DefaultBufferSize
  }

  // helper for the edge case for undefined content type
  private[response] def guessMediaTypeFromContent(request: Request, status: Status, byteArray: Array[Byte]): MediaType = {
    val successfulGetUrlExtension =
      if (status.isSuccess && request.isGet) request.href.extension
      else None
    if (successfulGetUrlExtension.isDefined) {
      MimeTypeRegistry.table.get(successfulGetUrlExtension.get) getOrElse guessMediaTypeFromBodyData(byteArray)
    } else {
      guessMediaTypeFromBodyData(byteArray)
    }
  }

  private[response] def guessMediaTypeFromBodyData(byteArray: Array[Byte]): MediaType = {
    if (byteArray.length == 0) {
      APPLICATION_OCTET_STREAM

    } else {
      var maybeHtml = byteArray(0) == '<'
      var maybeText = true
      var i = 0
      while (i < byteArray.length) {
        val bi = byteArray(i).toInt
        if (Character.isISOControl(bi) && !Character.isWhitespace(bi)) {
          maybeText = false
          maybeHtml = false
          i = byteArray.length // break
        }
        i += 1
      }
      if (maybeHtml) TEXT_HTML else if (maybeText) TEXT_PLAIN else APPLICATION_OCTET_STREAM
    }
  }
}