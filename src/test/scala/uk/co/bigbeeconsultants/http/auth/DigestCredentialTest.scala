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

package uk.co.bigbeeconsultants.http.auth

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import header.AuthenticateValue
import uk.co.bigbeeconsultants.http.request.Request
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DigestCredentialTest extends FunSuite {

  // http://www.rfc-editor.org/rfc/rfc2617.txt section 3.5
  // The following example assumes that an access-protected document is
  //    being requested from the server via a GET request. The URI of the
  //    document is "http://www.nowhere.org/dir/index.html". Both client and
  //    server know that the username for this document is "Mufasa", and the
  //    password is "Circle Of Life" (with one space between each of the
  //    three words).

  val sample = """|Digest
                 |realm="testrealm@host.com",
                 |qop="auth,auth-int",
                 |nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
                 |opaque="5ccc069c403ebaf9f0171e9517f40e41"""".stripMargin
  val authenticateValue = AuthenticateValue(sample)
  val digestCredential = new DigestCredential("Mufasa", "Circle Of Life", authenticateValue, "0a4f113b")

  test("check authenticate value") {
    assert("Digest" === authenticateValue.authScheme)
    assert(4 === authenticateValue.parts.size)
    assert("testrealm@host.com" === authenticateValue.realm.get)
    assert("dcd98b7102dd2f0e8b11d0f600bfb0c093" === authenticateValue.nonce.get)
    assert("5ccc069c403ebaf9f0171e9517f40e41" === authenticateValue.opaque.get)
    assert(None === authenticateValue.algorithm)
    assert(List("auth", "auth-int") === authenticateValue.qop)
    assert(sample.replace('\n', ' ') === authenticateValue.toString)
    assert(authenticateValue.isValid)
    assert(!authenticateValue.stale)
    assert("""DigestCredential(Mufasa, *********, Digest realm="testrealm@host.com", qop="auth,auth-int", nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093", opaque="5ccc069c403ebaf9f0171e9517f40e41", 0a4f113b)"""
      === digestCredential.toString)
  }


//  test("rfc2617 example response calculation") {
//    assert("6629fae49393a05397450978507c4ef1" === digestCredential.response())
//  }


  test("rfc2617 complete example") {
    assert(
      """|Digest username="Mufasa",
        |realm="testrealm@host.com", nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
        |uri="/dir/index.html", qop=auth, nc=00000001, cnonce="0a4f113b",
        |response="6629fae49393a05397450978507c4ef1", opaque="5ccc069c403ebaf9f0171e9517f40e41"""".stripMargin.replace('\n', ' ')
        === digestCredential.toDigestAuthHeaderValue(Request.get("http://any/dir/index.html"), 1))
  }

}
