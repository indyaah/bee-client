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

import uk.co.bigbeeconsultants.http.util.HttpUtil._
import collection.immutable.ListMap

/**
 * Defines a WWW-Authenticate or Proxy-Authenticate header with a list of name-value pairs.
 */
case class AuthenticateValue(authScheme: String, parts: ListMap[String, String]) extends Value {

  // domain            = "domain" "=" <"> URI ( 1*SP URI ) <">
  // URI               = absoluteURI | abs_path
  // nonce             = "nonce" "=" quoted-string
  // opaque            = "opaque" "=" quoted-string
  // stale             = "stale" "=" ( "true" | "false" )
  // algorithm         = "algorithm" "=" ( "MD5" | "MD5-sess" | token )
  // qop-options       = "qop" "=" <"> 1#qop-value <">
  // qop-value         = "auth" | "auth-int" | token

  lazy val realm: Option[String] = unquoted("realm")

  lazy val domain: List[String] = unquoted("domain").map(split(_, ' ')).getOrElse(Nil)

  lazy val nonce: Option[String] = unquoted("nonce")

  lazy val opaque: Option[String] = unquoted("opaque")

  lazy val stale: Boolean = parts.get("stale").map(_.toLowerCase == "true").getOrElse(false)

  lazy val algorithm: Option[String] = unquoted("algorithm")

  lazy val qop: List[String] = unquoted("qop").map(split(_, ',')).getOrElse(Nil)

  lazy val isValid = {
    if (authScheme == "Basic") realm.isDefined
    else if (authScheme == "Digest") realm.isDefined && nonce.isDefined
    else false
  }

  private def unquoted(key: String) = parts.get(key).map(unquote(_))

  override lazy val value = authScheme + " " + parts.map(kv => kv._1 + "=" + kv._2).mkString(", ")
}

//---------------------------------------------------------------------------------------------------------------------

object AuthenticateValue {
  def apply(headerValue: String): AuthenticateValue = {
    val sections = divide(headerValue.replace('\n', ' '), ' ')

    val parts = splitQuoted(sections._2, ',') map {
      v => divide(v.trim, '=')
    } map {
      kv => (kv._1.toLowerCase -> kv._2)
    }

    new AuthenticateValue(sections._1.trim, ListMap() ++ parts)
  }
}