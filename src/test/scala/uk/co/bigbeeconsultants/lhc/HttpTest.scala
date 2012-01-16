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

import com.pyruby.stubserver.StubMethod
import com.pyruby.stubserver.StubServer
import org.junit.{Test, Before, AfterClass, BeforeClass}
import org.junit.Assert._
import java.net.URL


class HttpTest {
  import HttpTest._

  private var http: Http = null

  @Before def setUp() {
    http = new Http()
  }

  @Test def get_shouldReturnOK() {
    val url = "/some/url"
    val stubbedMethod = StubMethod.get(url)
    val json = """{"astring" : "the message" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, json)
    val response = http.get(new URL(baseUrl + url))
    assertEquals(MediaType.APPLICATION_JSON, response.contentType)
    assertEquals(json, response.body)
  }

}

object HttpTest {
  private val port = (java.lang.Math.random * 16000 + 10000).asInstanceOf[Int]
  private var baseUrl: String = null
  private var server: StubServer = null

  @BeforeClass def configure() {
    baseUrl = "http://localhost:" + port
    server = new StubServer(port)
    server.start()
  }

  @AfterClass def after() {
    server.stop()
  }
}