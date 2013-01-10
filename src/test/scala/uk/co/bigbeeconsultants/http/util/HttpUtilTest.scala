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

  test("unquote") {
    assert("" === HttpUtil.unquote(""))
    assert("\"" === HttpUtil.unquote("\""))
    assert("xyz" === HttpUtil.unquote("\"xyz\""))
    assert("xyz" === HttpUtil.unquote("xyz"))
  }

  test("split blank") {
    assert(List("") === HttpUtil.split("", ':'))
  }

  test("split sepOnly") {
    assert(List("", "") === HttpUtil.split(":", ':'))
  }

  test("split oneOnly") {
    assert(List("only") === HttpUtil.split("only", ':'))
  }

  test("split endOnly") {
    assert(List("only", "") === HttpUtil.split("only:", ':'))
  }

  test("split startOnly") {
    assert(List("", "only") === HttpUtil.split(":only", ':'))
  }

  test("split three") {
    assert(List("one", "two", "three") === HttpUtil.split("one:two:three", ':'))
  }

  test("split quoted blank") {
    assert(List("") === HttpUtil.splitQuoted("", ':'))
  }

  test("split quoted sepOnly") {
    assert(List("", "") === HttpUtil.splitQuoted(":", ':'))
  }

  test("split quoted oneOnly") {
    assert(List("only") === HttpUtil.splitQuoted("only", ':'))
  }

  test("split quoted endOnly") {
    assert(List("only", "") === HttpUtil.splitQuoted("only:", ':'))
  }

  test("split quoted startOnly") {
    assert(List("", "only") === HttpUtil.splitQuoted(":only", ':'))
  }

  test("split quoted three") {
    assert(List("one", "two", "three") === HttpUtil.splitQuoted("one:two:three", ':'))
  }

  test("split quoted containing seps") {
    assert(List("a=\"one\"", "b=\"two,two,two\"", "c=three", "d=\"four\"") === HttpUtil.splitQuoted("a=\"one\",b=\"two,two,two\",c=three,d=\"four\"", ','))
  }

  test("divide0") {
    assert(("", "") === HttpUtil.divide("", ':'))
  }

  test("divide1") {
    assert(("xyz", "") === HttpUtil.divide("xyz", ':'))
  }

  test("divide2") {
    assert(("aa", "bb:cc") === HttpUtil.divide("aa:bb:cc", ':'))
  }

  test("copy no content") {
    val baos = new ByteArrayOutputStream
    val count = HttpUtil.copyBytes(null, baos)
    val result = new String(baos.toByteArray)
    assert(0 === count)
    assert("" === result)
  }

  test("copyBytes short") {
    val str = "short string"
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val baos = new ByteArrayOutputStream
    val count = HttpUtil.copyBytes(bais, baos)
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
    val count = HttpUtil.copyBytes(bais, baos)
    val result = new String(baos.toByteArray)
    assert(bytes.length === count)
    assert(str === result)
  }

  test("copyArray short") {
    val str = "short string"
    val bytes = str.getBytes
    val baos = new ByteArrayOutputStream
    val count = HttpUtil.copyArray(bytes, baos)
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
    val count = HttpUtil.copyArray(bytes, baos)
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
    HttpUtil.copyText(bais, baos)
    val result = new String(baos.toByteArray)
    assert(str === result)
  }

  test("copyText with null input stream") {
    val baos = new ByteArrayOutputStream
    HttpUtil.copyText(null, baos)
    val result = new String(baos.toByteArray)
    assert("" === result)
  }

  test("copyText with null output stream") {
    val bytes = "hello".getBytes
    val bais = new ByteArrayInputStream(bytes)
    HttpUtil.copyText(bais, null)
  }

  test("copyString") {
    val sb = new StringBuilder
    for (i <- 1 to 5) sb.append(i + " this is my string of text which will be repeated many times.\n")
    val str = sb.toString()
    val baos = new ByteArrayOutputStream
    HttpUtil.copyString(str, baos)
    val result = new String(baos.toByteArray)
    assert(str === result)
  }

  test("copyString with null output stream") {
    HttpUtil.copyString("hello", null)
  }

  test("captureBytes") {
    val str = "short string"
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val captured = HttpUtil.captureBytes(HttpUtil.copyBytes(bais, _))
    assert(captured === bytes)
  }
}
