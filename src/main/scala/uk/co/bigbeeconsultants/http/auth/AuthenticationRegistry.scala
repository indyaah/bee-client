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

import scala.collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap
import uk.co.bigbeeconsultants.http.url.{Path, Href, Endpoint}
import uk.co.bigbeeconsultants.http.header.{AuthenticateValue, Header}
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.Response

final class AuthenticationRegistry(credentialSuite: CredentialSuite = CredentialSuite.Empty, cacheRealms: Boolean = false) {

  // Mutable state is required for
  // (a) automatic realm lookup to reduce network traffic by reducing 401s
  // (b) digest authentication nonce counters
  private val knownRealms = new ConcurrentHashMap[Endpoint, Set[RealmMapping]]().asScala
  private val nonces = new ConcurrentHashMap[String, NonceVal]().asScala

  def findRealmMappings(endpoint: Endpoint): Set[RealmMapping] = knownRealms.get(endpoint) getOrElse Set()

  def findRealmMappings(href: Href): Set[RealmMapping] = findRealmMappings(href.endpoint.get)

  def findRealmMappings(request: Request): Set[RealmMapping] = findRealmMappings(request.href)

  def findKnownAuthHeaderFromMappings(request: Request, realmMappings: Iterable[RealmMapping]): Option[Header] = {
    val url = request.href
    val matchingRealm: Option[String] = realmMappings find (rm => url.path.startsWith(rm.path)) map (_.realm)
    val matchingCredential: Option[Credential] = matchingRealm.flatMap(credentialSuite.credentials.get)
    matchingCredential map (_.toBasicAuthHeader)
  }

  def findKnownAuthHeader(request: Request): Option[Header] = {
    findKnownAuthHeaderFromMappings(request, findRealmMappings(request.href))
  }

  def processResponse(response: Response, existingRealmMappings: Set[RealmMapping] = Set.empty): Option[Header] = {
    val wwwAuthenticateHeaders = response.headers.wwwAuthenticateHdrs
    val authorizationHeader = mustAuthenticate(response.request, wwwAuthenticateHeaders)
    if (cacheRealms)
      updateKnownRealms(response.request.href, wwwAuthenticateHeaders, existingRealmMappings)
    authorizationHeader
  }

  private def mustAuthenticate(request: Request, wwwAuthenticateHeaders: List[AuthenticateValue]): Option[Header] = {
    wwwAuthenticateHeaders match {
      case wah :: xs if wah.authScheme == "Digest" =>
        val nonceValOption = nonces.get(wah.realm.get)
        val newNonceVal = nonceValOption match {
          case Some(nonceVal) if nonceVal.nonce == wah.nonce.get =>
            nonceVal.next
          case Some(nonceVal) =>
            NonceVal(wah.nonce.get, 1)
          case None =>
            NonceVal(wah.nonce.get, 1)
        }
        nonces update(wah.realm.get, newNonceVal)
        credentialSuite.digestAuthHeader(wah, request, newNonceVal.count)

      case wah :: xs if wah.authScheme == "Basic" =>
        credentialSuite.basicAuthHeader(wah, request, 0)

      case _ => None
    }
  }

  private def updateKnownRealms(url: Href, wwwAuthenticateHeaders: List[AuthenticateValue], existingRealmMappings: Set[RealmMapping]) {
    for (wah <- wwwAuthenticateHeaders) {
      val realm = wah.realm.get
      val (thisOne, others) = existingRealmMappings.partition(_.realm == realm)
      assert(!others.exists(_.realm == realm))
      if (thisOne.isEmpty || thisOne.head.path.startsWith(url.path)) {
        val newMapping = new RealmMapping(realm, url.path)
        val newSet = others + newMapping
        knownRealms.update(url.endpoint.get, newSet)
      }
    }
  }

  def isEmpty = knownRealms.isEmpty

  def size = knownRealms.size

  def clear() {
    knownRealms.clear()
    nonces.clear()
  }

  // a testing seam
  private[auth] def put(endpoint: Endpoint, mappings: Set[RealmMapping]) {
    knownRealms.put(endpoint, mappings)
  }
}

//---------------------------------------------------------------------------------------------------------------------

case class RealmMapping(realm: String, path: Path)

case class NonceVal(nonce: String, count: Int) {
  def next = copy(count = count + 1)
}
