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

package uk.co.bigbeeconsultants.http.response

/**Expresses an HTTP status line. */
case class Status(code: Int, message: String) {
  /**The category is 1=informational, 2=success, 3=redirect, 4=client error, 5=server error. */
  val category = code / 100

  def isInformational = category == 1

  def isSuccess = category == 2

  def isRedirection = category == 3

  def isClientError = category == 4

  def isServerError = category == 5

  /**
   * Specifies whether a body is allowed in the response to which this status is attached.
   * @return true if the body may possibly have content; false if the body cannot have content.
   */
  def isBodyAllowed: Boolean = !isInformational && !bodyForbidden

  private def bodyForbidden = code == 204 || code == 205 || code == 304
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Lists the official status codes as defined in RFC2616.
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1
 * <p>
 * Note that java.net.HttpURLConnection has non-standard status codes in some cases.
 */
object Status {

  private var lookupTable = new scala.collection.immutable.HashMap[Int, Status]()

  /*----- 1XX: informational -----*/
  val S100_Continue = create(100, "Continue")
  val S101_SwitchingProtocols = create(101, "Switching protocols")

  /*----- 2XX: success -----*/
  val S200_OK = create(200, "OK")
  val S201_Created = create(201, "Created")
  val S202_Accepted = create(202, "Accepted") // result of non-committal asynchronous request
  val S203_NotAuthoritative = create(203, "Not authoritative")
  val S204_NoContent = create(204, "No content")
  val S205_ResetContent = create(205, "Reset content")
  val S206_PartialContent = create(206, "Partial content")

  /*----- 3XX: relocation/redirect -----*/
  val S300_MultipleChoice = create(300, "Multiple choice")
  val S301_MovedPermanently = create(301, "Moved permanently")
  val S302_Found = create(302, "Found")
  val S303_SeeOther = create(303, "See other")
  val S304_NotModified = create(304, "Not modified")
  val S305_UseProxy = create(305, "Use proxy")
  val S307_MovedTemporarily = create(307, "Moved temporarily")

  /*----- 4XX: client error -----*/
  val S400_BadRequest = create(400, "Bad request")
  val S401_Unauthorized = create(401, "Unauthorized")
  val S402_PaymentRequired = create(402, "Payment required")
  val S403_Forbidden = create(403, "Forbidden")
  val S404_NotFound = create(404, "Not found")
  val S405_BadMethod = create(405, "Bad method")
  val S406_NotAcceptable = create(406, "Not acceptable")
  val S407_ProxyAuthRequired = create(407, "Proxy authentication required")
  val S408_ClientTimeout = create(408, "Client timeout")
  val S409_Conflict = create(409, "Conflict")
  val S410_Gone = create(410, "Gone")
  val S411_LengthRequired = create(411, "Length required")
  val S412_PreconditionFailed = create(412, "Precondition failed")
  val S413_EntityTooLarge = create(413, "Entity too large")
  val S414_RequestURITooLong = create(414, "Request URI too long")
  val S415_UnsupportedType = create(415, "Unsupported type")
  val S416_RangeNotSatisfiable = create(416, "Range not satisfiable")
  val S417_ExpectationFailed = create(417, "Expectation failed")

  /*----- 5XX: server error -----*/
  val S500_InternalServerError = create(500, "Internal server error")
  val S501_NotImplemented = create(501, "Not implemented")
  val S502_BadGateway = create(502, "Bad gateway")
  val S503_ServiceUnavailable = create(503, "Service unavailable")
  val S504_GatewayTimeout = create(504, "Gateway timeout")
  val S505_UnsupportedVersion = create(505, "Unsupported version")

  /**
   * Gets a standard status or creates a new status with a given code.
   */
  def apply(code: Int) = {
    val existing = lookupTable.get(code)
    existing.getOrElse(new Status(code, "Status " + code))
  }

  private def create(code: Int, message: String) = {
    val s = new Status(code, message)
    lookupTable += (code -> s)
    s
  }
}