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

import uk.co.bigbeeconsultants.http.util.Base64
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.url.PartialURL

case class Credential(username: String, password: String) {
  def basicAuthHeader: Header = {
    val value = username + ":" + password
    val credential = "Basic " + Base64.encodeBytes(value.getBytes("UTF-8"))
    AUTHORIZATION -> credential
  }
}

/**
 * Factory for construction of basic authentication headers.
 */
object BasicAuthentication {
  def apply(username: String, password: String): Header = {
    new Credential(username, password).basicAuthHeader
  }
}


case class CredentialSuite(credentials: Map[PartialURL, Credential]) {
  def basicAuthHeader(url: PartialURL): Option[Header] = {
    null // TODO
  }
}

object CredentialSuite {
  val empty = new CredentialSuite(Map())
}