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

package uk.co.bigbeeconsultants.http.request

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http.url.PartialURL

class URLMapperTest extends FunSuite {

  val upstreamStr = "http://wombat/zzz"
  val downstreamStr = "http://localhost/a/b/c"
  val upstreamBase = PartialURL(upstreamStr)
  val downstreamBase = PartialURL(downstreamStr)

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
      m.rewriteRequest("first href='http://wombat/zzz/' and second href='http://wombat/zzz/foo/' too"))
  }

  test("DefaultURLMapper.rewriteResponse") {
    val m = new DefaultURLMapper(upstreamBase, downstreamBase)
    assert("first href='http://wombat/zzz/' and second href='http://wombat/zzz/foo/' too" ===
      m.rewriteResponse("first href='http://localhost/a/b/c/' and second href='http://localhost/a/b/c/foo/' too"))
  }

}
