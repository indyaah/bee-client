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
import uk.co.bigbeeconsultants.http.request.Request
import java.security.MessageDigest

trait Credential {
  def username: String

  def password: String

  def toAuthHeader(entityBody: String): Header
}

case class BasicCredential(username: String, password: String) extends Credential {

  def toAuthHeader(unusedEntityBody: String = ""): Header = {
    val value = username + ":" + password
    val credential = "Basic " + Base64.encodeBytes(value.getBytes("UTF-8"))
    HeaderName.AUTHORIZATION -> credential
  }
}

case class DigestCredential(username: String,
                            password: String,
                            authenticate: AuthenticateValue,
                            request: Request,
                            cnonce: String,
                            nonceCount: Int) extends Credential {
  import DigestCredential._
  val nonceCountHex = "%08x".format(nonceCount)
  val method = request.method
  val path = request.url.getPath
  val realm = authenticate.realm.getOrElse("")
  val algorithm = authenticate.algorithm.getOrElse("md5").toLowerCase
  val nonce = authenticate.nonce.get
  val qop = authenticate.qop.headOption.map(_.toLowerCase)

  require(algorithm == "md5" || algorithm == "md5-sess", algorithm)

  def response(entityBody: String = ""): String = {
    val ha1 = digestCalcHA1SessionKey
    val hashedEntity = new Array[Byte](0) // TODO not yet implemented
    val res = digestCalcResponse(ha1, hashedEntity)
    val enc = cvtHexString(res)
    assert(enc.length == 32, enc)
    enc
  }

  def toAuthHeaderValue(entityBody: String = ""): String = {
    // qop, cnonce and nonce-count are all sent or none sent
    val qopCnonceString =
      if (authenticate.qop.nonEmpty) {
        ", qop=" + qop.get + ", nc=" + nonceCountHex + ", cnonce=" + quote(cnonce)
      }
      else ""

    val realmString = optionalPartQuoted(", realm=", authenticate.realm)
    val nonceString = optionalPartQuoted(", nonce=", authenticate.nonce)
    val uriString = ", uri=" + quote(path)
    val responseString = ", response=" + quote(response(entityBody))
    val algorithmString = optionalPart(", algorithm=", authenticate.algorithm)
    val opaqueString = optionalPartQuoted(", opaque=", authenticate.opaque)

    "Digest username=" + quote(username) + realmString + nonceString +
      uriString + qopCnonceString + responseString +
      algorithmString + opaqueString
  }

  def toAuthHeader(entityBody: String = ""): Header = {
    HeaderName.AUTHORIZATION -> toAuthHeaderValue(entityBody)
  }

  private def optionalPart(prefix: String, value: Option[String]) = if (value.isDefined) prefix + value.get else ""

  private def optionalPartQuoted(prefix: String, value: Option[String]) = if (value.isDefined) prefix + quote(value.get) else ""

  private def quote(s: String) = '"' + s + '"'

  private def cvtHexString(b: Array[Byte]): String = {
    val s = new Array[Char](b.length)
    for (i <- 0 until b.length) {
      s(i) = b(i).asInstanceOf[Char]
    }
    new String(s)
  }

  private def cvtHex(bin: Array[Byte]): Array[Byte] = {
    val hex = new Array[Byte](bin.length * 2)
    var j = 0
    for (c <- bin) {
      hex(j) = lcHexTable((c >> 4) & 0xf)
      j += 1
      hex(j) = lcHexTable(c & 0xf)
      j += 1
    }
    hex
  }

  /** calculate H(A1) as per spec */
  private def digestCalcHA1SessionKey: Array[Byte] = {
    val md5a = new MD5
    md5a.update(username)
    md5a.update(':')
    md5a.update(realm)
    md5a.update(':')
    md5a.update(password)
    val ha1 = if (algorithm == "md5-sess") {
      val md5b = new MD5
      md5b.update(cvtHex(md5a.digest))
      md5b.update(':')
      md5b.update(nonce)
      md5b.update(':')
      md5b.update(cnonce)
      md5b.digest
    }
    else {
      md5a.digest
    }
    cvtHex(ha1)
  }

  /**
   * calculate request-digest/response-digest as per HTTP Digest spec
   * @return request-digest or response-digest
   */
  private def digestCalcResponse(ha1: Array[Byte], hashedEntity: Array[Byte]): Array[Byte] = {

    // calculate H(A2)
    val md5ha1 = new MD5
    md5ha1.update(method)
    md5ha1.update(':')
    md5ha1.update(path) // digestUri
    if (qop == Some("auth-int")) {
      md5ha1.update(':')
      md5ha1.update(hashedEntity)
    }
    val ha2 = md5ha1.digest
    val ha2Hex = cvtHex(ha2)

    // calculate response
    val md5res = new MD5
    md5res.update(ha1)
    md5res.update(':')
    md5res.update(nonce)
    md5res.update(':')
    if (qop.isDefined) {
      md5res.update(nonceCountHex)
      md5res.update(':')
      md5res.update(cnonce)
      md5res.update(':')
      md5res.update(qop.get)
      md5res.update(':')
    }
    md5res.update(ha2Hex)
    cvtHex(md5res.digest)
  }

}

private class MD5 {
  private val md5 = MessageDigest.getInstance("MD5")

  def update(ba: Array[Byte]) {
    md5.update(ba)
  }

  def update(s: String) {
    md5.update(s.getBytes("UTF-8"))
  }

  def update(c: Char) {
    md5.update(c.asInstanceOf[Byte]) // n.b. may truncate
  }

  def digest = md5.digest()
}

object DigestCredential {
  val lcHexTable = "0123456789abcdef".getBytes("ASCII")

}