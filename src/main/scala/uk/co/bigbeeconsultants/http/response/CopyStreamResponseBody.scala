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
import uk.co.bigbeeconsultants.http.Util
import java.io.{OutputStream, InputStream}

/**
 * Provides a body implementation that copies the whole response from the response input stream into an output
 * stream. The data is not buffered more than necessary to copy it.
 */
final class CopyStreamResponseBody(outputStream: OutputStream) extends ResponseBody {
  private var _contentType: MediaType = _

  override def receiveData(contentType: MediaType, inputStream: InputStream) {
    _contentType = contentType
    Util.copyBytes(inputStream, outputStream)
    inputStream.close ()
  }

  def contentType: MediaType = _contentType
}


/**
 * Provides an implementation of ResponseBodyFactory that creates new CopyStreamResponseBody
 * instances for all media types, using the output stream provided in the constructor.
 * This might be used, for example, in copying the response back to to an HttpServletResponse.
 * Typically, this will need a new factory instance for every response output stream, which will be
 * used once only and discarded.
 */
class CopyStreamResponseBodyFactory(outputStream: OutputStream) extends ResponseBodyFactory {
  def newBody(contentType: MediaType) = new CopyStreamResponseBody(outputStream)
}
