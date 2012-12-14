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
import java.io.{InputStream, OutputStreamWriter, OutputStream}
import util.HttpUtil

/**
 * Carries body data on a request. The body data is supplied by a closure using the
 * target HTTP output stream as its parameter, allowing data to be streamed from an arbitrary
 * source in order to minimise memory footprint, if required.
 * <p>
 * The companion object provides apply methods for common sources of body data.
 */
final class RequestBody(val mediaType: MediaType, val copyTo: OutputStream => Unit, source: => Any = "...") {

  // TODO allow multipass access to source during digest authentication
  override def toString = "RequestBody(" + mediaType + "," + source.toString + ")"

  def toShortString = {
    val s = source.toString
    val sourceString = if (s.length > 125) s.substring(0, 125) + "..." else s
    "(" + mediaType + "," + sourceString + ")"
  }
}


/**
 * Factory for request bodies.
 */
object RequestBody {

  /**
   * Factory for request bodies sourced from strings.
   */
  def apply(string: String, mediaType: MediaType): RequestBody = {
    new RequestBody (mediaType, (outputStream) => {
      val encoding = mediaType.charsetOrElse (HttpClient.UTF8)
      outputStream.write (string.getBytes (encoding))
      outputStream.flush()
    }, string)
  }

  /**
   * Factory for request bodies sourced from key-value pairs, typical for POST requests.
   */
  def apply(data: Map[String, String], mediaType: MediaType = APPLICATION_FORM_URLENCODED): RequestBody = {
    new RequestBody (mediaType, (outputStream) => {
      val encoding = mediaType.charsetOrElse (HttpClient.UTF8)
      val w = new OutputStreamWriter (outputStream, encoding)
      var amp = ""
      for ((key, value) <- data) {
        w.append(amp)
        w.write (URLEncoder.encode (key, encoding))
        w.write ('=')
        w.write (URLEncoder.encode (value, encoding))
        amp = "&"
      }
      w.flush ()
    }, data)
  }

  /**
   * Factory for request bodies sourced from input streams. This copies the content from the input stream,
   * which it leaves unclosed.
   */
  def apply(inputStream: InputStream, mediaType: MediaType): RequestBody = {
    new RequestBody (mediaType, (outputStream) => {
      HttpUtil.copyBytes (inputStream, outputStream)
    })
  }

  /**
   * Factory for empty request bodies. An empty body differs from no body at all because it has a media type.
   */
  def apply(mediaType: MediaType): RequestBody = new RequestBody (mediaType, (outputStream) => {}, "")
}
