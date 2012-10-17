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

import uk.co.bigbeeconsultants.http.header.{AuthenticateValue, Header, HeaderName}
import uk.co.bigbeeconsultants.http.util.Base64

trait Credential {
  def toAuthHeader: Header
}

case class BasicCredential(username: String, password: String) extends Credential {

  def toAuthHeader: Header = {
    val value = username + ":" + password
    val credential = "Basic " + Base64.encodeBytes(value.getBytes("UTF-8"))
    HeaderName.AUTHORIZATION -> credential
  }

}

case class DigestCredential(username: String, password: String, authenticate: AuthenticateValue,
                            digestUri: String,
                            cnonce: Option[String],
                            messageQop: Option[String], nonceCount: Option[Int],
                            authParam: Option[String]) extends Credential {

  def toAuthHeader: Header = {
    // qop, cnonce and nonce-count are all sent or none sent
    val qopCnonce =
      if (authenticate.qop.nonEmpty) {
        val nc = "%08x".format(nonceCount.get)
        ",qop=" + messageQop.get + ",cnonce=" + cnonce.get + ",nc=" + nc
      }
      else ""
    val response = ""
    val credential = "Digest " +
      "username=" + quote(username) +
      authenticate.realm.map(",realm=" + _) +
      authenticate.nonce.map(",nonce=" + quote(_)) +
      ",uri=" + digestUri +
      qopCnonce +
      ",response=" + quote(response) +
      authenticate.algorithm.map(",algorithm=" + _) +
      authenticate.opaque.map(",opaque=" + quote(_))

    //Base64.encodeBytes(value.getBytes("UTF-8"))
    HeaderName.AUTHORIZATION -> credential
  }

  private def quote(s: String) = '"' + s + '"'
}

//  def respondToChallenge(challenge: AuthenticateValue): Header = {
//    challenge.authScheme match {
//      case "Basic" => basicAuthHeader
//      case "Digest" => digestAuthHeader(challenge)
//    }
//  }
