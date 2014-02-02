package uk.co.bigbeeconsultants.http.header

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

import org.scalatest.FunSuite

class HeaderNameTest extends FunSuite {

  test("header construction via arrow operator with string") {
    val hn = HeaderName("Fruit")
    val h = hn -> "Banana"
    assert("Fruit: Banana" === h.toString)
  }

  test("header construction via unicode arrow operator with string") {
    val hn = HeaderName("Fruit")
    val h = hn → "Banana"
    assert("Fruit: Banana" === h.toString)
  }

  test("header construction via arrow operator with number") {
    val hn = HeaderName("Flies")
    val h = hn -> 123
    assert("Flies: 123" === h.toString)
  }

  test("header construction via unicode arrow operator with number") {
    val hn = HeaderName("Flies")
    val h = hn → 123
    assert("Flies: 123" === h.toString)
  }

  test("header construction via arrow operator with Value") {
    val hn = HeaderName("Stuff")
    val h = hn -> MediaType.TEXT_HTML.withCharset("ASCII")
    assert("Stuff: text/html;charset=ASCII" === h.toString)
  }

  test("header construction via unicode arrow operator with Value") {
    val hn = HeaderName("Stuff")
    val h = hn → MediaType.TEXT_HTML.withCharset("ASCII")
    assert("Stuff: text/html;charset=ASCII" === h.toString)
  }

  test("header construction via arrow operator with HttpDateTimeInstant") {
    val hn = HeaderName.DATE
    val now = new HttpDateTimeInstant()
    val h = hn -> now
    assert("Date: " + now === h.toString)
  }

  test("header construction via unicode arrow operator with HttpDateTimeInstant") {
    val hn = HeaderName.DATE
    val now = new HttpDateTimeInstant()
    val h = hn → now
    assert("Date: " + now === h.toString)
  }

  test("HeaderName,String equals ignore case") {
    val hn1 = HeaderName("Stuff")
    val hn2 = "STUFF"
    assert(hn1 =~= hn2)
  }

  test("HeaderName,HeaderName equals ignore case") {
    val hn1 = HeaderName("Stuff")
    val hn2 = HeaderName("STUFF")
    assert(hn1 != hn2)
    assert(hn1 =~= hn2)
  }

  test("HeaderName,Header equals ignore case") {
    val hn1 = HeaderName("Stuff")
    val h2 = HeaderName("STUFF") -> "yay"
    assert(hn1 != h2)
    assert(hn1 =~= h2)
  }
}
