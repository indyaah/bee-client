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
  lazy val category = code / 100

  def isInformational = category == 1

  def isSuccess = category == 2

  def isRedirection = category == 3

  def isClientError = category == 4

  def isServerError = category == 5
}

/**
 * Lists the official status codes as defined in RFC2616.
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1
 * <p>
 * Note that java.net.HttpURLConnection has non-standard status codes
 * in some cases.
 */
object Status {

  /*----- 1XX: informational -----*/
  val S100_Continue = Status(100, "Continue")
  val S101_SwitchingProtocols = Status(101, "Switching protocols")

  /*----- 2XX: success -----*/
  val S200_OK = Status(200, "OK")
  val S201_Created = Status(201, "Created")
  val S202_Accepted = Status(202, "Accepted")
  val S203_NotAuthoritative = Status(203, "Not authoritative")
  val S204_NoContent = Status(204, "No content")
  val S205_ResetContent = Status(205, "Reset content")
  val S206_PartialContent = Status(206, "Partial content")

  /*----- 3XX: relocation/redirect -----*/
  val S300_MultipleChoice = Status(300, "Multiple choice")
  val S301_MovedPermanently = Status(301, "Moved permanently")
  val S302_Found = Status(302, "Found")
  val S303_SeeOther = Status(303, "See other")
  val S304_NotModified = Status(304, "Not modified")
  val S305_UseProxy = Status(305, "Use proxy")
  val S307_MovedTemporarily = Status(307, "Moved temporarily")

  /*----- 4XX: client error -----*/
  val S400_BadRequest = Status(400, "Bad request")
  val S401_Unauthorized = Status(401, "Unauthorized")
  val S402_PaymentRequired = Status(402, "Payment required")
  val S403_Forbidden = Status(403, "Forbidden")
  val S404_NotFound = Status(404, "Not found")
  val S405_BadMethod = Status(405, "Bad method")
  val S406_NotAcceptable = Status(406, "Not acceptable")
  val S407_ProxyAuthRequired = Status(407, "Proxy authentication required")
  val S408_ClientTimeout = Status(408, "Client timeout")
  val S409_Conflict = Status(409, "Conflict")
  val S410_Gone = Status(410, "Gone")
  val S411_LengthRequired = Status(411, "Length required")
  val S412_PreconditionFailed = Status(412, "Precondition failed")
  val S413_EntityTooLarge = Status(413, "Entity too large")
  val S414_RequestURITooLong = Status(414, "Request URI too long")
  val S415_UnsupportedType = Status(415, "Unsupported type")
  val S416_RangeNotSatisfiable = Status(416, "Range not satisfiable")
  val S417_ExpectationFailed = Status(417, "Expectation failed")

  /*----- 5XX: server error -----*/
  val S500_InternalServerError = Status(500, "Internal server error")
  val S501_NotImplemented = Status(501, "Not implemented")
  val S502_BadGateway = Status(502, "Bad gateway")
  val S503_ServiceUnavailable = Status(503, "Service unavailable")
  val S504_GatewayTimeout = Status(504, "Gateway timeout")
  val S505_UnsupportedVersion = Status(505, "Unsupported version")
}