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

package uk.co.bigbeeconsultants.http

import org.scalatest.FunSuite
import java.net.URL
import uk.co.bigbeeconsultants.http.response.Status
import uk.co.bigbeeconsultants.http.header.MediaType

class OddCasesTest extends FunSuite {

  val config = Config(connectTimeout = 20000)

  // this duplicates testing of ByteBufferResponseBody.apply
  test("empty content-length should not blow up") {
    val httpClient = new HttpClient(config)
    val response = httpClient.get(new URL("http://www.lansdowneclub.com/robots.txt"))
    assert(response.status === Status.S200_OK)
    assert(response.body.contentType.value === MediaType.TEXT_PLAIN.value)
    assert(response.body.contentLength === 0)
  }
}
