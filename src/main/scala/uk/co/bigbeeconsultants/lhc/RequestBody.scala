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

import java.io.{OutputStreamWriter, OutputStream}
import java.net.URLEncoder

/**
 * Carries body data on a request. The body data is supplied by a closure using the
 * HTTP output stream as its parameter, allowing data to be streamed from an arbitrary
 * source in order to minimise memory footprint, if required.
 * <p>
 * The companion object provides apply methods for common sources of body data.
 */
class RequestBody(val mediaType: MediaType, val copyTo: OutputStream => Unit)


/** Factory for request bodies. */
object RequestBody {

  /** Factory for request bodies sourced from strings. */
  def apply(mediaType: MediaType, string: String): RequestBody = {
    new RequestBody(mediaType, (outputStream) => {
      val encoding = mediaType.charsetOrElse(HttpClient.defaultCharset)
      outputStream.write(string.getBytes(encoding))
    })
  }

  /** Factory for request bodies sourced from key-value pairs, typical for POST requests. */
  def apply(mediaType: MediaType, data: Map[String, String]): RequestBody = {
    new RequestBody(mediaType, (outputStream) => {
      val encoding = mediaType.charsetOrElse(HttpClient.defaultCharset)
      val w = new OutputStreamWriter(outputStream, encoding)
      var first = true
      for ((key, value) <- data) {
        if (!first) w.append('&')
        else first = false
        w.write(URLEncoder.encode(key, encoding))
        w.write('=')
        w.write(URLEncoder.encode(value, encoding))
      }
      w.close()
    })
  }
}
