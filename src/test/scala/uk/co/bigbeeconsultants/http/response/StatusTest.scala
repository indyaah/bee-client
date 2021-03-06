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

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StatusTest extends FunSuite with ShouldMatchers {

  test("isInformational") {
    assert(Status.S100_Continue.isInformational)
    assert(!Status.S200_OK.isInformational)
  }

  test("isSuccess") {
    assert(!Status.S100_Continue.isSuccess)
    assert(Status.S200_OK.isSuccess)
  }

  test("isRedirection") {
    assert(!Status.S100_Continue.isRedirection)
    assert(Status.S301_MovedPermanently.isRedirection)
  }

  test("isClientError") {
    assert(!Status.S100_Continue.isClientError)
    assert(Status.S400_BadRequest.isClientError)
  }

  test("isServerError") {
    assert(!Status.S100_Continue.isServerError)
    assert(Status.S500_InternalServerError.isServerError)
  }

  test("isBodyAllowed") {
    assert(!Status.S100_Continue.isBodyAllowed)
    assert(!Status.S204_NoContent.isBodyAllowed)
    assert(!Status.S205_ResetContent.isBodyAllowed)
    assert(!Status.S304_NotModified.isBodyAllowed)
    assert(Status.S200_OK.isBodyAllowed)
    assert(Status.S500_InternalServerError.isBodyAllowed)
  }

  test("Single-arg apply method") {
    assert(Status(200) eq Status.S200_OK)
    assert(Status(500) eq Status.S500_InternalServerError)
  }

  test("toString") {
    assert(Status.S200_OK.toString === "Status(200,OK)")
  }

  test("equals") {
    assert(Status(500) === Status.S500_InternalServerError)
    assert(Status(200) === new Status(200, "Random"))
    assert(Status(500) != Status.S200_OK)
    assert(Status(500) != "string")
  }

  test("hashcode") {
    val set = new mutable.HashSet[Int]()
    for (i <- 100 to 550) {
      val status = new Status(i, "")
      val first = status.hashCode()
      assert(!set.contains(first))
      set += status.hashCode()
      assert(first === status.hashCode())
    }
  }

  test("unapply") {
    Status.S200_OK match {
      case Status(c, m) =>
        assert(c === 200)
        assert(m === "OK")
      case _ =>
        fail()
    }
  }
}