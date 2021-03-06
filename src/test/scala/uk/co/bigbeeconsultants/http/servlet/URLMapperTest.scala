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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import uk.co.bigbeeconsultants.http.url.Href
import uk.co.bigbeeconsultants.http.util.HttpUtil._

@RunWith(classOf[JUnitRunner])
class URLMapperTest extends FunSuite {

  val upstreamStr = "http://wombat.co.uk/zzz"
  val downstreamStr = "http://localhost/a/b/c"
  val upstreamBase = Href(upstreamStr)
  val downstreamBase = Href(downstreamStr)

  test("DefaultURLMapper.mapToDownstream") {
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)
    assert(downstreamStr + "/hello/world#at?red=1" === m.mapToDownstream(upstreamStr + "/hello/world#at?red=1").toString)
  }

  test("DefaultURLMapper.mapToUpstream") {
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)
    assert(upstreamStr + "/hello/world#at?red=1" === m.mapToUpstream(downstreamStr + "/hello/world#at?red=1").toString)
  }

  test("DefaultURLMapper.rewriteRequest") {
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)
    assert("first href='http://localhost/a/b/c/' and second href='http://localhost/a/b/c/foo/' too" ===
      m.rewriteRequest("first href='http://wombat.co.uk/zzz/' and second href='http://wombat.co.uk/zzz/foo/' too"))
  }

  test("DefaultURLMapper.rewriteResponse") {
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)
    assert("first href='http://wombat.co.uk/zzz/' and second href='http://wombat.co.uk/zzz/foo/' too" ===
      m.rewriteResponse("first href='http://localhost/a/b/c/' and second href='http://localhost/a/b/c/foo/' too"))
  }

  test("using json sample with no more than one url per line") {
    val upstreamStr = "http://localhost:8080"
    val downstreamStr = "http://prodserver.widgets.org/app"
    val upstreamBase = Href(upstreamStr)
    val downstreamBase = Href(downstreamStr)
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)

    val localhostJson = loadFile("samples/aCustomer1.json")
    val prodServerJson = loadFile("samples/aCustomer2.json")
    val reqRewritten = m.rewriteRequest(localhostJson)
    val resRewritten = m.rewriteResponse(prodServerJson)
    assert(prodServerJson === reqRewritten)
    assert(resRewritten === localhostJson)
  }

  private def loadFile(name: String) = {
    val is = getClass.getClassLoader.getResourceAsStream(name)
    assert(is != null, name)
    new String(copyToByteArrayAndClose(is, 8192), "UTF-8")
  }
}
