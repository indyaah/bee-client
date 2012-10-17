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

class AuthenticateValueTest extends FunSuite {


  test("basic string") {
    val v = AuthenticateValue("Basic realm=\"private\"")
    assert("Basic" === v.authScheme)
    assert(1 === v.parts.size)
    assert("private" === v.realm.get)
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
  }

  test("rfc2617 example") {
    // http://www.rfc-editor.org/rfc/rfc2617.txt section 3.5
    // The following example assumes that an access-protected document is
    //    being requested from the server via a GET request. The URI of the
    //    document is "http://www.nowhere.org/dir/index.html". Both client and
    //    server know that the username for this document is "Mufasa", and the
    //    password is "Circle Of Life" (with one space between each of the
    //    three words).

    val s = "Digest\nrealm=\"testrealm@host.com\",\nqop=\"auth,auth-int\",\n" +
      "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",\nopaque=\"5ccc069c403ebaf9f0171e9517f40e41\""
    val v = AuthenticateValue(s)
    assert("Digest" === v.authScheme)
    assert(4 === v.parts.size)
    assert("testrealm@host.com" === v.realm.get)
    assert("dcd98b7102dd2f0e8b11d0f600bfb0c093" === v.nonce.get)
    assert("5ccc069c403ebaf9f0171e9517f40e41" === v.opaque.get)
    assert(None === v.algorithm)
    assert(List("auth", "auth-int") === v.qop)
    assert(s.replace('\n', ' ') === v.toString)
    assert(v.isValid)

//    "Authorization: Digest username=\"Mufasa\",\n" +
//      "realm=\"testrealm@host.com\",\nnonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",\n" +
//      "uri=\"/dir/index.html\",\nqop=auth,\nnc=00000001,\ncnonce=\"0a4f113b\",\n" +
//      "response=\"6629fae49393a05397450978507c4ef1\",\nopaque=\"5ccc069c403ebaf9f0171e9517f40e41\""
  }

}
