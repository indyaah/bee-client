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

package uk.co.bigbeeconsultants.http.url

case class Path(isAbsolute: Boolean = true,
                segments: List[String] = Nil) extends Seq[String] {

  override def apply(idx: Int) = segments(idx)

  override def iterator = segments.iterator

  override def length = segments.length

  override def toList = segments

  override def toString: String =
    if (isAbsolute) segments.mkString("/", "/", "")
    else segments.mkString("/")
}

object Path {
  val empty = new Path(false, Nil)

  def apply(path: String) = {
    if (path.isEmpty) empty
    else if (path == "/") new Path(true, Nil)
    else if (path(0) == '/') new Path(true, List() ++ path.substring(1).split("/"))
    else new Path(false, List() ++ path.split("/"))
  }
}
