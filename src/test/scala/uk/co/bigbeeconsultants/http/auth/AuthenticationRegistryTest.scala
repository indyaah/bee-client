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
import uk.co.bigbeeconsultants.http.request.Request
import java.net.URL
import uk.co.bigbeeconsultants.http.header.{AuthenticateValue, Headers, MediaType}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.response.{EmptyResponseBody, Status, Response}
import uk.co.bigbeeconsultants.http.url.{PartialURL, Endpoint}
import collection.immutable.ListMap

class AuthenticationRegistryTest extends FunSuite {

  val fredBloggs = new Credential("fred", "bloggs")
  val johnSmith = new Credential("john", "smith")
  val fbRealm = "FBRealm"
  val jsRealm = "JSRealm"
  val credentials = new CredentialSuite(Map(fbRealm -> fredBloggs, jsRealm -> johnSmith))
  val getExampleOneTwo = Request.get(new URL("http://example.com/one/two"))
  val getExampleTwo = Request.get(new URL("http://example.com/two"))
  val getExampleOne = Request.get(new URL("http://example.com/one"))
  val exampleCom = getExampleOne.split.endpoint.get

  test("empty registry with no authentication requirement") {
    val request = getExampleOneTwo
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    val realmMappings = authenticationRegistry.findRealmMappings(request)
    assert(realmMappings.isEmpty)

    val authHeader1 = authenticationRegistry.findKnownAuthHeaderFromMappings(request, realmMappings)
    assert(authHeader1.isEmpty)

    val response = Response(request, Status.S204_NoContent, new EmptyResponseBody(MediaType.APPLICATION_JSON), Headers(), None)
    val authHeader2 = authenticationRegistry.processResponse(response, realmMappings)
    assert(authHeader2.isEmpty)
  }

  test("findRealmMappings should return nothing with non-matching endpoint") {
    val request = getExampleOneTwo
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(Endpoint("http://w3.org/"), Set(RealmMapping(fbRealm, request.split.path)))

    val realmMappings = authenticationRegistry.findRealmMappings(request)
    assert(realmMappings.isEmpty)
  }

  test("findRealmMappings should return nothing with matching endpoint but empty list") {
    val request = getExampleOneTwo
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, Set())

    val realmMappings = authenticationRegistry.findRealmMappings(request)
    assert(realmMappings.isEmpty)

    val authHeader = authenticationRegistry.findKnownAuthHeaderFromMappings(request, realmMappings)
    assert(authHeader.isEmpty)
  }

  test("findRealmMappings should return existing list with matching endpoint") {
    val request = getExampleOneTwo
    val mappings = Set(RealmMapping(fbRealm, request.split.path))
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, mappings)

    val realmMappings = authenticationRegistry.findRealmMappings(request)
    assert(realmMappings === mappings)
  }

  test("findKnownAuthHeader should return nothing with non-matching realm") {
    val request = getExampleOneTwo
    val mappings = Set(RealmMapping("Nowhere", request.split.path))
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, mappings)

    val authHeader = authenticationRegistry.findKnownAuthHeader(request)
    assert(authHeader.isEmpty)
  }

  test("findKnownAuthHeader should return nothing with non-matching path") {
    val request = getExampleOneTwo
    val mappings = Set(RealmMapping(fbRealm, PartialURL("http://w3.org/a/b/c").path))
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, mappings)

    val authHeader = authenticationRegistry.findKnownAuthHeader(request)
    assert(authHeader.isEmpty)
  }

  test("findKnownAuthHeader should return existing list with matching endpoint") {
    val request = getExampleOneTwo
    val mappings = Set(RealmMapping(fbRealm, request.split.path))
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, mappings)

    val authHeader = authenticationRegistry.findKnownAuthHeader(request)
    assert(authHeader === Some(fredBloggs.toBasicAuthHeader))
  }

  test("processResponse should return nothing if the response contains no challenges") {
    val request = getExampleOneTwo
    val mappings = Set(RealmMapping(fbRealm, request.split.path))
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, mappings)

    val response = Response(request, Status.S204_NoContent, new EmptyResponseBody(MediaType.APPLICATION_JSON), Headers(), None)
    val authHeader = authenticationRegistry.processResponse(response, mappings)
    assert(authHeader.isEmpty)
  }

  test("processResponse should return nothing if the response challenge is not satisfied by the credentials") {
    val request = getExampleOneTwo
    val mappings = Set(RealmMapping(fbRealm, request.split.path))
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request.split.endpoint.get, mappings)

    val challengeValue = AuthenticateValue("Basic realm=\"Somewhere Else\"")
    assert(challengeValue.isValid)
    val challenge = WWW_AUTHENTICATE -> challengeValue.toString
    val response = Response(request, Status.S401_Unauthorized, new EmptyResponseBody(MediaType.APPLICATION_JSON), Headers(challenge), None)
    val authHeader = authenticationRegistry.processResponse(response, mappings)
    assert(authHeader.isEmpty)
  }

  test("processResponse should return authorisation header if the response challenge is satisfied by the credentials and the size should increase") {
    val request1 = getExampleOneTwo
    val request2 = getExampleOne
    val fbMapping = RealmMapping(fbRealm, request1.split.path)
//    val jsMapping = RealmMapping(jsRealm, getExampleTwo.split.path)
    val mappings = Set(fbMapping)
    val authenticationRegistry = new AuthenticationRegistry(credentials, true)
    authenticationRegistry.put(request1.split.endpoint.get, mappings)
    assert(authenticationRegistry.findRealmMappings(exampleCom) === Set(RealmMapping(fbRealm, request1.split.path)))

    val challengeValue = new AuthenticateValue("Basic", ListMap("realm" -> fbRealm))
    assert(challengeValue.isValid)
    val challenge = WWW_AUTHENTICATE -> challengeValue.toString
    val response1 = Response(request1, Status.S401_Unauthorized, new EmptyResponseBody(MediaType.APPLICATION_JSON), Headers(challenge), None)
    val authHeader1 = authenticationRegistry.processResponse(response1, mappings)
    assert(authHeader1 === Some(fredBloggs.toBasicAuthHeader))
    assert(authenticationRegistry.size == 1)
    assert(authenticationRegistry.findRealmMappings(exampleCom) === Set(RealmMapping(fbRealm, request1.split.path)))

    val response2 = Response(request2, Status.S401_Unauthorized, new EmptyResponseBody(MediaType.APPLICATION_JSON), Headers(challenge), None)
    val authHeader = authenticationRegistry.processResponse(response2, mappings)
    assert(authHeader === Some(fredBloggs.toBasicAuthHeader))
    assert(authenticationRegistry.size == 1)
    assert(authenticationRegistry.findRealmMappings(exampleCom) === Set(RealmMapping(fbRealm, request2.split.path)))
  }

  //TODO more tests for digest authentication
}
