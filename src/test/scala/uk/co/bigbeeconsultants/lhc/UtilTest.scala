package uk.co.bigbeeconsultants.lhc

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

import org.junit.Test
import org.junit.Assert._
import javax.xml.bind.DatatypeConverter
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class UtilTest {

  @Test
  def split_blank() {
    assertEquals(List(""), Util.split("", ':'))
  }

  @Test
  def split_sepOnly() {
    assertEquals(List("", ""), Util.split(":", ':'))
  }

  @Test
  def split_oneOnly() {
    assertEquals(List("only"), Util.split("only", ':'))
  }

  @Test
  def split_endOnly() {
    assertEquals(List("only", ""), Util.split("only:", ':'))
  }

  @Test
  def split_startOnly() {
    assertEquals(List("", "only"), Util.split(":only", ':'))
  }

  @Test
  def split_three() {
    assertEquals(List("one", "two", "three"), Util.split("one:two:three", ':'))
  }

  @Test
  def divide0() {
    assertEquals(("", ""), Util.divide("", ':'))
  }

  @Test
  def divide1() {
    assertEquals(("xyz", ""), Util.divide("xyz", ':'))
  }

  @Test
  def divide2() {
    assertEquals(("aa", "bb:cc"), Util.divide("aa:bb:cc", ':'))
  }

  @Test
  def copyBytesShort() {
    val str = "short string"
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val baos = new ByteArrayOutputStream
    val count = Util.copyBytes(bais, baos)
    val result = new String(baos.toByteArray)
    assertEquals(bytes.length, count)
    assertEquals(result, str)
  }

  @Test
  def copyBytesLong() {
    val sb = new StringBuilder
    for (i <- 1 to 10000) sb.append("this is my string of text which will be repeated many times.")
    val str = sb.toString()
    val bytes = str.getBytes
    val bais = new ByteArrayInputStream(bytes)
    val baos = new ByteArrayOutputStream
    val count = Util.copyBytes(bais, baos)
    val result = new String(baos.toByteArray)
    assertEquals(bytes.length, count)
    assertEquals(result, str)
  }
}
