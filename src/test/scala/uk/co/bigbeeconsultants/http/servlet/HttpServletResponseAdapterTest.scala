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

package uk.co.bigbeeconsultants.http.servlet

import org.scalatest.FunSuite
import org.mockito.Mockito._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.header.{MediaType, Headers}
import javax.servlet.http.HttpServletResponse
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.request.Request
import java.net.URL
import uk.co.bigbeeconsultants.http.response.Status

class HttpServletResponseAdapterTest extends FunSuite {

  test("HttpServletResponseAdapter setResponseHeaders") {
    val servletResponse = mock(classOf[HttpServletResponse])
    val adapter = new HttpServletResponseAdapter(servletResponse, None)
    adapter.setResponseHeaders(Headers(HOST -> "krum"))
    verify(servletResponse).setHeader("Host", "krum")
  }

  test("HttpServletResponseAdapter copy content") {
    val s = """So shaken as we are, so wan with care!"""
    val inputStream = new ByteArrayInputStream(s.getBytes(HttpClient.UTF8))
    val request = Request.get(new URL("http://krum/"))
    val servletResponse = mock(classOf[HttpServletResponse])
    val sos = new CaptureOutputStream
    when(servletResponse.getOutputStream) thenReturn sos

    val adapter = new HttpServletResponseAdapter(servletResponse, None)
    adapter.responseBuilder.captureResponse(request, Status.S200_OK, Some(MediaType.TEXT_PLAIN), Headers(), None, inputStream)
    adapter.sendResponse()
    verify(servletResponse).setStatus(200, "OK")
    verify(servletResponse, times(2)).getOutputStream
    assert(new String(sos.toByteArray) === s)
    verifyNoMoreInteractions(servletResponse)
  }
}
