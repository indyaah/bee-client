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

import collection.{JavaConversions, mutable}
import uk.co.bigbeeconsultants.http.url.{Path, PartialURL, Endpoint}
import java.util.concurrent.ConcurrentHashMap
import uk.co.bigbeeconsultants.http.header.{AuthenticateValue, Header}
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.Response
import uk.co.bigbeeconsultants.http.header.HeaderName._

private[http] class AuthenticationRegistry(credentialSuite: CredentialSuite = CredentialSuite.empty) {
  private val knownRealms: mutable.ConcurrentMap[Endpoint, Set[RealmMapping]] = JavaConversions.asScalaConcurrentMap(new ConcurrentHashMap[Endpoint, Set[RealmMapping]]())

  def findRealmMappings(endpoint: Endpoint): Set[RealmMapping] = knownRealms.get(endpoint) getOrElse Set()

  def findRealmMappings(request: Request): Set[RealmMapping] = findRealmMappings(request.split.endpoint.get)

  def findKnownAuthHeaderFromMappings(request: Request, realmMappings: Iterable[RealmMapping]): Option[Header] = {
    val url = request.split
    val matchingRealm: Option[Realm] = realmMappings find (rm => url.path.startsWith(rm.path)) map (_.realm)
    val matchingCredential: Option[Credential] = matchingRealm flatMap (credentialSuite.credentials.get(_))
    matchingCredential map (_.toBasicAuthHeader)
  }

  def findKnownAuthHeader(request: Request): Option[Header] = {
    findKnownAuthHeaderFromMappings(request, findRealmMappings(request))
  }

  def processResponse(request: Request, response: Response, existingRealmMappings: Set[RealmMapping]): Option[Header] = {
    val wwwAuthenticateHeaders: List[AuthenticateValue] = response.headers.filter(WWW_AUTHENTICATE).list map (_.toAuthenticateValue)
    val authorizationHeader = mustAuthenticate(request, wwwAuthenticateHeaders)
    updateKnownRealms(request.split, wwwAuthenticateHeaders, existingRealmMappings)
    authorizationHeader
  }

  private def mustAuthenticate(request: Request, wwwAuthenticateHeaders: List[AuthenticateValue]): Option[Header] = {
    val possibleAuthorizationHeaders = for {
      wah <- wwwAuthenticateHeaders
      authorizationHeader = credentialSuite.authHeader(wah)
      if authorizationHeader.isDefined
    } yield {
      authorizationHeader.get
    }
    possibleAuthorizationHeaders.headOption
  }

  private def updateKnownRealms(url: PartialURL, wwwAuthenticateHeaders: List[AuthenticateValue], existingRealmMappings: Set[RealmMapping]) {
    for (wah <- wwwAuthenticateHeaders) {
      val realm = wah.realm.get
      val (thisOne, others) = existingRealmMappings.partition(_.realm.realm == realm)
      assert(!others.exists(_.realm.realm == realm))
      if (thisOne.isEmpty || thisOne.head.path.startsWith(url.path)) {
        val newMapping = new RealmMapping(Realm(realm), url.path)
        val newSet = others + newMapping
        knownRealms.update(url.endpoint.get, newSet)
      }
    }
  }

  def isEmpty = knownRealms.isEmpty

  def size = knownRealms.size

  def clear() {
    knownRealms.clear()
  }

  private[auth] def put(endpoint: Endpoint, mappings: Set[RealmMapping]) {
    knownRealms.put(endpoint, mappings)
  }
}

private[auth] case class RealmMapping(realm: Realm, path: Path)