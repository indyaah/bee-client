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
import java.io.ByteArrayInputStream
import java.net.URL
import javax.servlet.http.HttpServletResponse
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.header.{MediaType, Headers}
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.Status
import uk.co.bigbeeconsultants.http.url.Href
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.util.DiagnosticTimer

class HttpServletResponseAdapterTest extends FunSuite {

  test("HttpServletResponseAdapter setResponseHeaders") {
    val servletResponse = mock(classOf[HttpServletResponse])
    val adapter = new HttpServletResponseAdapter(servletResponse, None)
    adapter.setResponseHeaders(Headers(HOST -> "krum"))
    verify(servletResponse).setHeader("Host", "krum")
  }

  test("HttpServletResponseAdapter copy content") {
    val downstreamContent =
      "So shaken as we are, so wan with care! link:'http://target.myco.co.uk/base/content/example.json'\n"
    val upstreamContent =
      "So shaken as we are, so wan with care! link:'http://localhost:8080/content/example.json'\n"
    val inputStream = new ByteArrayInputStream(downstreamContent.getBytes(HttpClient.UTF8))
    val request = Request.get(new URL("http://krum/"))
    val servletResponse = mock(classOf[HttpServletResponse])
    val sos = new CaptureOutputStream
    when(servletResponse.getOutputStream) thenReturn sos

    val upstreamStr = "http://localhost:8080"
    val downstreamStr = "http://target.myco.co.uk/base"
    val upstreamBase = Href(upstreamStr)
    val downstreamBase = Href(downstreamStr)
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)

    val responseBodyFilter = TextualBodyFilter(m.rewriteResponse, AllTextualMediaTypes)
    val adapter = new HttpServletResponseAdapter(servletResponse, Some(responseBodyFilter))
    val headers = Headers(CONTENT_LENGTH -> downstreamContent.length.toString)
    adapter.responseBuilder.captureResponse(request, Status.S200_OK, Some(MediaType.TEXT_PLAIN), headers, None, inputStream, new DiagnosticTimer)
    adapter.sendResponse()
    verify(servletResponse).setStatus(200, "OK")
    verify(servletResponse).setHeader(CONTENT_LENGTH.name, upstreamContent.length.toString)
    verify(servletResponse, times(2)).getOutputStream
    assert(new String(sos.toByteArray) === upstreamContent)
    verifyNoMoreInteractions(servletResponse)
  }
}
