package uk.co.bigbeeconsultants.http

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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.scalatest.FunSuite

class UtilTest extends FunSuite {

  test ("split_blank") {
    expect (List (""))(Util.split ("", ':'))
  }

  test ("split_sepOnly") {
    expect (List ("", ""))(Util.split (":", ':'))
  }

  test ("split_oneOnly") {
    expect (List ("only"))(Util.split ("only", ':'))
  }

  test ("split_endOnly") {
    expect (List ("only", ""))(Util.split ("only:", ':'))
  }

  test ("split_startOnly") {
    expect (List ("", "only"))(Util.split (":only", ':'))
  }

  test ("split_three") {
    expect (List ("one", "two", "three"))(Util.split ("one:two:three", ':'))
  }

  test ("divide0") {
    expect (("", ""))(Util.divide ("", ':'))
  }

  test ("divide1") {
    expect (("xyz", ""))(Util.divide ("xyz", ':'))
  }

  test ("divide2") {
    expect (("aa", "bb:cc"))(Util.divide ("aa:bb:cc", ':'))
  }

  test ("copyBytesShort") {
    val str = "short string"
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream (bytes)
    val baos = new ByteArrayOutputStream
    val count = Util.copyBytes (bais, baos)
    val result = new String (baos.toByteArray)
    expect (bytes.length)(count)
    expect (result)(str)
  }

  test ("copyBytesLong") {
    val sb = new StringBuilder
    for (i <- 1 to 10000) sb.append ("this is my string of text which will be repeated many times.")
    val str = sb.toString ()
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream (bytes)
    val baos = new ByteArrayOutputStream
    val count = Util.copyBytes (bais, baos)
    val result = new String (baos.toByteArray)
    expect (bytes.length)(count)
    expect (result)(str)
  }
}
