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

package uk.co.bigbeeconsultants.http.header

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.auth.DigestCredential
import uk.co.bigbeeconsultants.http.request.Request
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AuthenticateValueTest extends FunSuite {

  test("basic string") {
    val v = AuthenticateValue("Basic realm=\"private\"")
    assert("Basic" === v.authScheme)
    assert(1 === v.parts.size)
    assert("private" === v.realm.get)
    assert("Basic realm=\"private\"" === v.value)
    assert("Basic realm=\"private\"" === v.toString)
    assert(v.isValid)
  }


  test("digest string") {
    val v = AuthenticateValue("Digest realm=\"private\", nonce=\"DCKawjTMBAA=d61b9a5f7be110cda76e46e3ac032bdccd440fae\", algorithm=MD5, qop=\"auth\"")
    assert("Digest" === v.authScheme)
    assert(4 === v.parts.size)
    assert("private" === v.realm.get)
    assert("DCKawjTMBAA=d61b9a5f7be110cda76e46e3ac032bdccd440fae" === v.nonce.get)
    assert("MD5" === v.algorithm.get)
    assert(List("auth") === v.qop)
    assert("Digest realm=\"private\", nonce=\"DCKawjTMBAA=d61b9a5f7be110cda76e46e3ac032bdccd440fae\", algorithm=MD5, qop=\"auth\"" === v.toString)
    assert(v.isValid)
    assert(!v.stale)
  }
}
