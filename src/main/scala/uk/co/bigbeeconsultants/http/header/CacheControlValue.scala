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
import org.slf4j.LoggerFactory

//TODO
case class CacheControlValue(value: String, isValid: Boolean, label: String, deltaSeconds: Option[Int], fieldName: Option[String])
  extends Value {
  require(deltaSeconds.isEmpty || fieldName.isEmpty, "Cannot define both deltaSeconds and fieldName")
}


object CacheControlValue {
  private val logger = LoggerFactory.getLogger(getClass)

  def apply(value: String): CacheControlValue = {
    if (value.isEmpty)
      new CacheControlValue(value, false, value, None, None)
    else {
      val (a, b) = divide(value, '=')
      try {
        if (b.isEmpty) {
          new CacheControlValue(value, true, a, None, None)
        } else if (b(0) == '"') {
          new CacheControlValue(value, true, a, None, Some(unquote(b)))
        } else {
          val number = Some(b.toInt)
          // Number values are always positive integral numbers in HTTP headers.
          val valid = number.isEmpty || number.get >= 0
          new CacheControlValue(value, valid, a, number, None)
        }
      } catch {
        case e: NumberFormatException =>
          logger.debug("{}: failed to parse number. {}", Array(value, e.getMessage))
          new CacheControlValue(value, false, a, None, None)
      }
    }
  }

  def apply(label: String, deltaSeconds: Option[Int], fieldName: Option[String]) = {
    if (deltaSeconds.isDefined) new CacheControlValue(label + "=" + deltaSeconds.get, true, label, deltaSeconds, None)
    else if (fieldName.isDefined) new CacheControlValue(label + "=\"" + fieldName.get + "\"", true, label, None, fieldName)
    else new CacheControlValue(label, true, label, None, fieldName)
  }

  def ifValid(value: String) = {
    val v = apply(value)
    if (v.isValid) Some(v) else None
  }

  /** Constructs a new max-age instance. */
  def maxAge(deltaSeconds: Int) = apply("max-age", Some(deltaSeconds), None)

  /** Constructs a new max-stale instance. */
  def maxStale(deltaSeconds: Option[Int]) = apply("max-stale", deltaSeconds, None)

  /** Constructs a new min-fresh instance. */
  def minFresh(deltaSeconds: Int) = apply("min-fresh", Some(deltaSeconds), None)

  /** The no-cache instance. */
  val NoCache = apply("no-cache", None, None)

  /** The no-store instance. */
  val NoStore = apply("no-store", None, None)

  /** The only-if-cached instance. */
  val OnlyIfCached = apply("only-if-cached", None, None)

  /** The no-transform instance. */
  val NoTransform = apply("no-transform", None, None)
}