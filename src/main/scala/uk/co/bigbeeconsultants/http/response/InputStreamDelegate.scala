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

import java.io.{IOException, InputStream}
import java.net.HttpURLConnection

/**
 * Constructs an a wrapper for an HTTP input stream that ensures that its parent HTTP connection
 * is disconnected when the input stream is closed.
 * @param in the input stream; if null then the wrapper will behave as if zero-length was provided.
 * @param connection the parent connection for the input stream.
 */
class InputStreamDelegate(in: InputStream, connection: HttpURLConnection) extends InputStream {

  /**
   * Reads the next byte of data from the input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream
   * has been reached, the value <code>-1</code> is returned. This method
   * blocks until input data is available, the end of the stream is detected,
   * or an exception is thrown.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached.
   * @throws IOException  if an I/O error occurs.
   */
  @throws(classOf[IOException])
  def read(): Int =
    if (in == null) -1
    else in.read()

  /**
   * Closes this input stream and releases any system resources associated with the stream.
   * Then the HttpURLConnection is disconnected, releasing any system resources associated with it.
   *
   * @throws IOException  if an I/O error occurs.
   */
  @throws(classOf[IOException])
  override def close() {
    try {
      if (in != null)
        in.close()
    }
    finally {
      connection.disconnect()
    }
  }
}
