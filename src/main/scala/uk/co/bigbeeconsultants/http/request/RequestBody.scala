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

package uk.co.bigbeeconsultants.http.request

import java.net.URLEncoder
import uk.co.bigbeeconsultants.http._
import header.MediaType
import header.MediaType._
import java.io.{InputStream, OutputStream}
import util.HttpUtil

/**
 * Carries body data on a request. The body data is supplied by a closure using the
 * target HTTP output stream as its parameter, allowing data to be streamed from an arbitrary
 * source in order to minimise memory footprint, if required.
 * <p>
 * The companion object provides apply methods for common sources of body data.
 */
trait RequestBody {
  /** Gets the function that consumes this request body. */
  def copyTo: OutputStream => Unit

  /** Gets the content type. */
  def contentType: MediaType

  /** Gets a string representation of the body, if possible. */
  def asString: String

  /** Gets the string representation and the content type for diagnostic purposes. */
  final def toShortString = "(" + asString + "," + contentType + ")"

  override def toString = "RequestBody" + toShortString
}


/**
 * Factory for request bodies.
 */
object RequestBody {

  /**
   * Factory for empty request bodies. An empty body differs from no body at all because it has a media type.
   */
  def apply(contentType: MediaType): RequestBody = new StringRequestBody("", contentType)

  /**
   * Factory for request bodies sourced from strings.
   */
  def apply(string: String, contentType: MediaType): RequestBody = new StringRequestBody(string, contentType)

  /**
   * Factory for request bodies sourced from key-value pairs, typical for POST requests.
   */
  def apply(data: Map[String, String], contentType: MediaType = APPLICATION_FORM_URLENCODED): RequestBody = {
    val b = new StringBuilder
    val encoding = contentType.charsetOrElse(HttpClient.UTF8)
    var amp = ""
    for ((key, value) <- data) {
      b.append(amp)
      b.append(URLEncoder.encode(key, encoding))
      b.append('=')
      b.append(URLEncoder.encode(value, encoding))
      amp = "&"
    }
    new StringRequestBody(b.toString, contentType)
  }

  /**
   * Factory for request bodies sourced from binary data.
   */
  def apply(byteArray: Array[Byte], contentType: MediaType): RequestBody = new BinaryRequestBody(byteArray, contentType)

  /**
   * Factory for request bodies sourced from input streams. This copies the content from the input stream,
   * which it leaves unclosed.
   */
  def apply(inputStream: InputStream, contentType: MediaType): RequestBody =
    new StreamRequestBody((outputStream) => HttpUtil.copyBytes(inputStream, outputStream), contentType)
}


final class StringRequestBody(string: String, val contentType: MediaType) extends RequestBody {
  def copyTo: (OutputStream) => Unit =
    (outputStream) => {
      val encoding = contentType.charsetOrElse(HttpClient.UTF8)
      outputStream.write(string.getBytes(encoding))
      outputStream.flush()
    }

  def asString = if (string.length > 125) string.substring(0, 125) + "..." else string
}


final class BinaryRequestBody(byteArray: Array[Byte], val contentType: MediaType) extends RequestBody {
  def copyTo: (OutputStream) => Unit =
    (outputStream) => {
      outputStream.write(byteArray)
      outputStream.flush()
    }

  def asString = "..."
}


final class StreamRequestBody(copyToFn: (OutputStream) => Unit, val contentType: MediaType) extends RequestBody {
  private var consumed = false

  def copyTo = {
    if (consumed) throw new IllegalStateException("Cannot use the copyTo function more than once.")
    consumed = true
    copyToFn
  }

  def asString = "..."
}