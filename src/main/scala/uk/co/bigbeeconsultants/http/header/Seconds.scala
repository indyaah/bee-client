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

import java.util.Date

/**
 * Simply represents a time duration in seconds, corresponding to the timestamp accurate in HTTP header values.
 * As with `java.util.Date`, it can also represent the time elapsed since the JVM epoch 1st January 1970.
 */
case class Seconds(seconds: Long) extends Ordered[Seconds] {
  def +(s: Long) = Seconds(seconds + s)

  def -(s: Long) = Seconds(seconds - s)

  def +(s: Seconds) = Seconds(seconds + s.seconds)

  def -(s: Seconds) = Seconds(seconds - s.seconds)

  def *(m: Long) = Seconds(seconds * m)

  def /(d: Long) = Seconds(seconds / d)

  def milliseconds = seconds * 1000

  def max(other: Seconds) = if (this.seconds > other.seconds) this else other

  override val toString = seconds + "s"

  /** Implements ordering of instances. */
  def compare(that: Seconds) = seconds.compare(that.seconds)
}

object Seconds {
  def apply(date: Date) = new Seconds(date.getTime / 1000)

  def sinceEpoch = new Seconds(System.currentTimeMillis() / 1000)

  val Zero = new Seconds(0)
}
