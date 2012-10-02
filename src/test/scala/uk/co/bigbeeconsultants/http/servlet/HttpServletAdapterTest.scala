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
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.header.{MediaType, Headers}
import java.{util => ju}
import org.mockito.Mockito._
import javax.servlet.http.HttpServletResponse
import uk.co.bigbeeconsultants.http.util.StubHttpServletRequest
import uk.co.bigbeeconsultants.http.url.{Path, PartialURL}

class HttpServletAdapterTest extends FunSuite {

  test("HttpServletRequestAdapter.url") {

    val splitUrl = PartialURL(Some("http"), Some("localhost"), None, Path("/context/x/y/z"), None, Some("a=1"))
    val s = splitUrl.toString
    val req = new StubHttpServletRequest().copyFrom(splitUrl)
    req.contentType = MediaType.TEXT_PLAIN.value

    val adapterUrl = new HttpServletRequestAdapter(req).url
    assert(splitUrl === adapterUrl)
  }

  test("HttpServletRequestAdapter.requestBody") {
    val headers = Headers(List(HOST -> "localhost", ACCEPT -> "foo", ACCEPT_LANGUAGE -> "en", CONTENT_TYPE -> "text/plain"))
    val req = new StubHttpServletRequest().copyFrom(headers)
    req.contentType = MediaType.TEXT_PLAIN.value

    val adapter = new HttpServletRequestAdapter(req)

    assert(headers === adapter.headers)
    assert(MediaType.TEXT_PLAIN === adapter.requestBody.mediaType)
  }

  test("HttpServletResponseAdapter setResponseHeaders") {
    val res = mock(classOf[HttpServletResponse])
    val adapter = new HttpServletResponseAdapter(res)
    adapter.setResponseHeaders(Headers(HOST -> "krum"))
    verify(res).setHeader("Host", "krum")
  }

  //  test("CopyStreamResponseBody") {
  //    val s = """So shaken as we are, so wan with care!"""
  //    val baos = new ByteArrayOutputStream
  //    val inputStream = new ByteArrayInputStream(s.getBytes(HttpClient.UTF8))
  //    val mt = MediaType.TEXT_PLAIN
  //    val body = new CopyStreamResponseBody(baos, mt, inputStream)
  //
  //    body.contentType should be(mt)
  //    val result = new String(baos.toByteArray, HttpClient.UTF8)
  //    result should be (s)
  //  }


  test("convertRequestHeaders") {

  }


  test("copyResponse") {

  }
}
