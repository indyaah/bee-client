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

/**
 * Represents a file path or URL path as a sequence of strings.
 *
 * Note that standard sequence operations are available, especially head, tail, init, last, take, drop.
 * @param isAbsolute true if rooted at '/'
 * @param segments the segments separated by '/'
 */
class Path(val isAbsolute: Boolean = true,
           val segments: List[String] = Nil) extends Seq[String] {

  // Note: this is not a case class because, were it so, the Seq trait would provide an incorrect equals method.

  override def init = new Path(isAbsolute, segments.init)

  override def tail = new Path(false, segments.tail)

  override def take(n: Int) = new Path(isAbsolute, segments.take(n))

  override def drop(n: Int) = new Path(false, segments.drop(n))

  override def apply(idx: Int) = segments(idx)

  override def iterator = segments.iterator

  override def length = segments.length

  override def toList = segments

  override def toString: String =
    if (isAbsolute) segments.mkString("/", "/", "")
    else segments.mkString("/")

  /** Tests whether this path starts with the segments found in another path. */
  def startsWith(other: Path) = {
    this.isAbsolute == other.isAbsolute &&
      this.length >= other.length &&
      this.segments.slice(0, other.length) == other.segments
  }

  /** Concatenates this path with another, which must be relative. */
  def +(relative: Path): Path = {
    require(!relative.isAbsolute, relative)
    new Path(this.isAbsolute, segments ++ relative.segments)
  }

  /** Concatenates this path with another path segment. */
  def /(seg: String): Path = {
    new Path(this.isAbsolute, segments ++ List(seg))
  }

  override def hashCode() = segments.hashCode()

  override def equals(that: Any) = {
    if (that == null || !that.isInstanceOf[Path]) false
    else {
      val other = that.asInstanceOf[Path]
      if (this eq other) true
      else
        this.isAbsolute == other.isAbsolute &&
          this.segments == other.segments
    }
  }
}

/**
 * Represents a file path or URL path as a sequence of strings.
 */
object Path {
  val empty = new Path(false, Nil)
  val root = new Path(false, Nil)

  /** Constructs an instance directly. */
  def apply(isAbsolute: Boolean, segments: List[String]) = new Path(isAbsolute, segments)

  /** Constructs an instance from a string such as "/a/b/c" or "d/e/f". */
  def apply(path: String) = {
    if (path.isEmpty) empty
    else if (path == "/") new Path(true, Nil)
    else if (path(0) == '/') new Path(true, List() ++ path.substring(1).split("/"))
    else new Path(false, List() ++ path.split("/"))
  }
}
