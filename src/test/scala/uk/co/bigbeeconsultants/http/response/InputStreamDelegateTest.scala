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

import org.scalatest.FunSuite
import org.mockito.Mockito._
import java.net.HttpURLConnection
import java.io.{IOException, InputStream}

class InputStreamDelegateTest extends FunSuite {

  test("read method should call read on the underlying stream") {
    val is = mock(classOf[InputStream])
    val connection = mock(classOf[HttpURLConnection])
    val isd = new InputStreamDelegate(is, connection)

    isd.read()

    verify(is).read()
    verifyZeroInteractions(connection)
  }

  test("null input stream should not cause failure") {
    val is: InputStream = null
    val connection = mock(classOf[HttpURLConnection])
    val isd = new InputStreamDelegate(is, connection)

    isd.read()
    isd.close()

    verify(connection).disconnect()
  }

  test("close method should close the underlying stream and also disconnect the connection") {
    val is = mock(classOf[InputStream])
    val connection = mock(classOf[HttpURLConnection])
    val isd = new InputStreamDelegate(is, connection)

    isd.close()

    verify(is).close()
    verify(connection).disconnect()
  }

  test("close method should disconnect the connection even if it fails to close the underlying stream") {
    val is = mock(classOf[InputStream])
    val connection = mock(classOf[HttpURLConnection])
    val isd = new InputStreamDelegate(is, connection)
    when(is.close()) thenThrow new IOException()

    intercept[IOException] {
      isd.close()
    }

    verify(is).close()
    verify(connection).disconnect()
  }
}