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

package uk.co.bigbeeconsultants.http.util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.scalatest.FunSuite

class HttpUtilTest extends FunSuite {

  import HttpUtil._

  test("quote") {
    assert("\"\"" === quote(""))
    assert("\"abc\"" === quote("abc"))
    intercept[IllegalArgumentException] { quote("abc\"def") }
  }

  test("unquote") {
    assert("" === unquote(""))
    assert("\"" === unquote("\""))
    assert("xyz" === unquote("\"xyz\""))
    assert("xyz" === unquote("xyz"))
  }

  test("split blank") {
    assert(List("") === split("", ':'))
  }

  test("split sepOnly") {
    assert(List("", "") === split(":", ':'))
  }

  test("split oneOnly") {
    assert(List("only") === split("only", ':'))
  }

  test("split endOnly") {
    assert(List("only", "") === split("only:", ':'))
  }

  test("split startOnly") {
    assert(List("", "only") === split(":only", ':'))
  }

  test("split three") {
    assert(List("one", "two", "three") === split("one:two:three", ':'))
  }

  test("split quoted blank") {
    assert(List("") === splitQuoted("", ':'))
  }

  test("split quoted sepOnly") {
    assert(List("", "") === splitQuoted(":", ':'))
  }

  test("split quoted oneOnly") {
    assert(List("only") === splitQuoted("only", ':'))
  }

  test("split quoted endOnly") {
    assert(List("only", "") === splitQuoted("only:", ':'))
  }

  test("split quoted startOnly") {
    assert(List("", "only") === splitQuoted(":only", ':'))
  }

  test("split quoted three") {
    assert(List("one", "two", "three") === splitQuoted("one:two:three", ':'))
  }

  test("split quoted containing seps") {
    assert(List("a=\"one\"", "b=\"two,two,two\"", "c=three", "d=\"four\"") === splitQuoted("a=\"one\",b=\"two,two,two\",c=three,d=\"four\"", ','))
  }

  test("divide0") {
    assert(("", "") === divide("", ':'))
  }

  test("divide1") {
    assert(("xyz", "") === divide("xyz", ':'))
  }

  test("divide2") {
    assert(("aa", "bb:cc") === divide("aa:bb:cc", ':'))
  }

  test("copy no content") {
    val baos = new ByteArrayOutputStream
    val count = copyBytes(null, baos)
    val result = new String(baos.toByteArray)
    assert(0 === count)
    assert("" === result)
  }

  test("copyBytes short") {
    val str = "short string"
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val baos = new ByteArrayOutputStream
    val count = copyBytes(bais, baos)
    val result = new String(baos.toByteArray)
    assert(bytes.length === count)
    assert(str === result)
  }

  test("copyBytes long") {
    val sb = new StringBuilder
    for (i <- 1 to 10000) sb.append(i + " this is my string of text which will be repeated many times.\n")
    val str = sb.toString()
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val baos = new ByteArrayOutputStream
    val count = copyBytes(bais, baos)
    val result = new String(baos.toByteArray)
    assert(bytes.length === count)
    assert(str === result)
  }

  test("copyArray short") {
    val str = "short string"
    val bytes = str.getBytes
    val baos = new ByteArrayOutputStream
    val count = copyArray(bytes, baos)
    val result = new String(baos.toByteArray)
    assert(bytes.length === count)
    assert(str === result)
  }

  test("copyArray long") {
    val sb = new StringBuilder
    for (i <- 1 to 10000) sb.append(i + " this is my string of text which will be repeated many times.\n")
    val str = sb.toString()
    val bytes = str.getBytes
    val baos = new ByteArrayOutputStream
    val count = copyArray(bytes, baos)
    val result = new String(baos.toByteArray)
    assert(bytes.length === count)
    assert(str === result)
  }

  test("copyText") {
    val sb = new StringBuilder
    for (i <- 1 to 5) sb.append(i + " this is my string of text which will be repeated many times.\n")
    val str = sb.toString()
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val baos = new ByteArrayOutputStream
    copyText(bais, baos)
    val result = new String(baos.toByteArray)
    assert(str === result)
  }

  test("copyText with null input stream") {
    val baos = new ByteArrayOutputStream
    copyText(null, baos)
    val result = new String(baos.toByteArray)
    assert("" === result)
  }

  test("copyText with null output stream") {
    val bytes = "hello".getBytes
    val bais = new ByteArrayInputStream(bytes)
    copyText(bais, null)
  }

  test("copyString") {
    val sb = new StringBuilder
    for (i <- 1 to 5) sb.append(i + " this is my string of text which will be repeated many times.\n")
    val str = sb.toString()
    val baos = new ByteArrayOutputStream
    copyString(str, baos)
    val result = new String(baos.toByteArray)
    assert(str === result)
  }

  test("copyString with null output stream") {
    copyString("hello", null)
  }

  test("captureBytes") {
    val str = "short string"
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val captured = captureBytes(copyBytes(bais, _))
    assert(captured === bytes)
  }
}
